package it.lisan.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

private val Forest = Color(0xFF123B35)
private val Deep = Color(0xFF0B2927)
private val Teal = Color(0xFF246F60)
private val Mint = Color(0xFFDDF1E9)
private val Gold = Color(0xFFE6B96B)
private val Ivory = Color(0xFFF5F4EE)
private val Paper = Color(0xFFFFFEFA)
private val Muted = Color(0xFF596B63)
private val android.content.Context.store by preferencesDataStore("lisan_progress")
private val XP = intPreferencesKey("xp")
private val DONE = stringSetPreferencesKey("done")
private val PROFESSOR_NOTES = stringSetPreferencesKey("professor_notes")
private val EGY = intPreferencesKey("mix_egy")
private val MOR = intPreferencesKey("mix_mor")
private val ALG = intPreferencesKey("mix_alg")
private val TUN = intPreferencesKey("mix_tun")
private val privateDefaults = mapOf("egy" to 75, "mor" to 0, "alg" to 0, "tun" to 0)
private data class Mix(val egy:Int=75,val mor:Int=0,val alg:Int=0,val tun:Int=0) {
    val msa:Int get() = (100-egy-mor-alg-tun).coerceAtLeast(0)
}
private data class Phrase(val ar:String,val latin:String,val it:String)
private data class Lesson(val id:String,val title:String,val dialect:String,val key:String,val phrases:List<Phrase>)
private val lessons = listOf(
    Lesson("eg1","I primi saluti","EGIZIANO","egy",listOf(Phrase("إزيك؟","izzayyak? / izzayyik?","Come stai?"),Phrase("أنا كويس","ana kwayyis","Sto bene (uomo)"),Phrase("الحمد لله","el-ḥamdu lillāh","Grazie a Dio"),Phrase("شكراً","shokran","Grazie"))),
    Lesson("eg2","Presentarsi","EGIZIANO","egy",listOf(Phrase("أنا اسمي ...","ana ismi ...","Mi chiamo ..."),Phrase("إنت اسمك إيه؟","inta ismak eh?","Come ti chiami?"),Phrase("أنا من إيطاليا","ana min iṭālya","Vengo dall'Italia"))),
    Lesson("ma1","Saluti marocchini","MAROCCHINO · DARIJA","mor",listOf(Phrase("السلام، لاباس؟","s-slam, labas?","Ciao, tutto bene?"),Phrase("كنبغي نتعلم الدارجة","kanbghi ntʿellem ddarija","Voglio imparare la darija"),Phrase("شكراً بزاف","shukran bzzaf","Grazie mille"),Phrase("بسلامة","bslama","Arrivederci"))),
    Lesson("ma2","Le prime risposte","MAROCCHINO · DARIJA","mor",listOf(Phrase("لاباس، الحمد لله","labas, lḥamdullah","Tutto bene, grazie a Dio"),Phrase("واخا","wakha","Va bene"),Phrase("فين كاين؟","fin kayn?","Dov'è?"))),
    Lesson("dz1","Saluti algerini","ALGERINO · DARIJA","alg",listOf(Phrase("واش راك؟","wach rak? / wach raki?","Come stai?"),Phrase("راني مليح","rani mlih","Sto bene"),Phrase("صحيت","saḥḥit","Grazie"),Phrase("بزاف","bzzaf","Molto / tanto"))),
    Lesson("dz2","Parole quotidiane","ALGERINO · DARIJA","alg",listOf(Phrase("واش هذا؟","wach hada?","Che cos'è questo?"),Phrase("ما فهمتش","ma fhemtsh","Non ho capito"),Phrase("نشوفك من بعد","nshufek men baʿd","Ci vediamo dopo"))),
    Lesson("tn1","Saluti tunisini","TUNISINO · DERJA","tun",listOf(Phrase("عسلامة، شنية أحوالك؟","ʿaslema, shnowa aḥwelek?","Ciao, come stai?"),Phrase("لاباس","labes","Tutto bene"),Phrase("يعطيك الصحة","yaʿtik eṣ-ṣaḥḥa","Grazie"),Phrase("برشة","barsha","Molto / tanto"))),
    Lesson("tn2","Nella conversazione","TUNISINO · DERJA","tun",listOf(Phrase("شنوة اسمك؟","shnowa esmek?","Come ti chiami?"),Phrase("أنا من إيطاليا","ena men Italia","Vengo dall'Italia"),Phrase("قدّاش؟","qaddech?","Quanto costa?"))),
    Lesson("msa1","Saluti standard","ARABO STANDARD","msa",listOf(Phrase("السلام عليكم","as-salāmu ʿalaykum","La pace sia con voi"),Phrase("صباح الخير","ṣabāḥ al-khayr","Buongiorno"),Phrase("مساء الخير","masāʾ al-khayr","Buonasera")))
)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        window.statusBarColor=android.graphics.Color.rgb(18,59,53)
        window.navigationBarColor=android.graphics.Color.rgb(245,244,238)
        setContent { App() }
    }
}

@Composable private fun App(){
    val ctx=LocalContext.current
    val scope= rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf("Home") }
    var selected by rememberSaveable { mutableStateOf("") }
    val progress by ctx.store.data.map { (it[XP]?:0) to (it[DONE]?: emptySet()) }.collectAsState(initial=0 to emptySet())
    val mix by ctx.store.data.map { Mix(it[EGY]?:75,it[MOR]?:0,it[ALG]?:0,it[TUN]?:0) }.collectAsState(initial=Mix())
    val xp=progress.first
    val done=progress.second
    val activeLessons=lessons.filter { l -> when(l.key) {
        "egy" -> mix.egy>0
        "mor" -> mix.mor>0
        "alg" -> mix.alg>0
        "tun" -> mix.tun>0
        else -> mix.msa>0
    } }
    val lesson=lessons.firstOrNull{it.id==selected}
        ?: createEndlessLessons(activeLessons, 1000).firstOrNull { it.id==selected }
    MaterialTheme(colorScheme=lightColorScheme(primary=Forest,onPrimary=Color.White,secondary=Teal,background=Ivory,surface=Paper,onSurface=Forest)){
        Scaffold(
            containerColor=Ivory,
            bottomBar={ if(lesson==null) NavigationBar(containerColor=Paper,contentColor=Forest) {
                listOf("Home","Impara","Voce","Professore","Profilo").forEach { t ->
                    NavigationBarItem(selected=page==t,onClick={page=t},icon={
                        Icon(when(t){"Home"->Icons.Default.Home;"Impara"->Icons.Default.MenuBook;"Voce"->Icons.Default.Mic;"Professore"->Icons.Default.School;else->Icons.Default.Tune},t)
                    },label={Text(if(t=="Professore") "Prof." else t,fontSize=11.sp,fontWeight=if(page==t) FontWeight.Bold else FontWeight.Medium,maxLines=1)},alwaysShowLabel=true,colors=NavigationBarItemDefaults.colors(selectedIconColor=Teal,indicatorColor=Mint,selectedTextColor=Forest,unselectedTextColor=Muted))
                }
            } }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                if(lesson!=null){
                    LessonScreen(lesson, alreadyCompleted=done.contains(lesson.id), onBack={selected=""}, onComplete={
                        scope.launch { ctx.store.edit { v -> val d=(v[DONE]?:emptySet()).toMutableSet(); if(d.add(lesson.id))v[XP]=(v[XP]?:0)+20;v[DONE]=d } }
                        selected="";page="Home"
                    })
                } else when(page){
                    "Home" -> HomeScreen(xp,done.size,mix.msa,onLearn={page="Impara"},onTalk={page="Voce"},onSettings={page="Profilo"},onReview={page="Ripasso"},onProfessor={page="Professore"})
                    "Impara" -> LearnScreen(activeLessons, mix, done, onSelect={selected=it})
                    "Voce" -> Speak(mix)
                    "Professore" -> ProfessorScreen()
                    "Ripasso" -> ReviewScreen(done.size,(lessons + createEndlessLessons(activeLessons,1000)).distinctBy{it.id}.filter{done.contains(it.id)},onSelect={selected=it})
                    else -> ProfileScreen(mix,onMixChange={newMix -> scope.launch { ctx.store.edit { p -> p[EGY]=newMix.egy;p[MOR]=newMix.mor;p[ALG]=newMix.alg;p[TUN]=newMix.tun } }})
                }
            }
        }
    }
}

@Composable private fun BrandMark(modifier:Modifier=Modifier,size:Int=54){
    Box(modifier.size(size.dp).clip(RoundedCornerShape((size/3).dp)).background(Brush.linearGradient(listOf(Teal,Forest))),contentAlignment=Alignment.Center){
        Text("ل",color=Gold,fontSize=(size*.64f).sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
        Box(Modifier.align(Alignment.BottomEnd).padding((size*.14f).dp).size((size*.12f).dp).background(Gold,CircleShape))
    }
}
@Composable private fun Header(eyebrow:String,title:String,subtitle:String?=null){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Text(eyebrow.uppercase(),color=Teal,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.1.sp,lineHeight=15.sp)
            Text(title,color=Forest,fontSize=26.sp,fontWeight=FontWeight.ExtraBold,lineHeight=31.sp)
            if(subtitle!=null) Text(subtitle,color=Muted,fontSize=14.sp,lineHeight=20.sp)
        }
        BrandMark(size=42)
    }
}
@Composable private fun HomeScreen(xp:Int,completed:Int,msa:Int,onLearn:()->Unit,onTalk:()->Unit,onSettings:()->Unit,onReview:()->Unit,onProfessor:()->Unit){
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        item { Header("Benvenuto in LISAN","Marḥaba.","L'arabo, con la voce e le parole di ogni giorno.") }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(28.dp),modifier=Modifier.fillMaxWidth()){
                Box(Modifier.background(Brush.linearGradient(listOf(Forest,Deep))).padding(22.dp)){
                    Column(verticalArrangement=Arrangement.spacedBy(14.dp)){
                        Row(verticalAlignment=Alignment.CenterVertically){Text("IL TUO PERCORSO",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp);Spacer(Modifier.weight(1f));Text("LIVELLO A0",color=Mint,fontSize=11.sp)}
                        Text("Una lingua viva.\nQuattro voci da scoprire.",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold,lineHeight=31.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                            StatPill("$completed lezioni",Icons.Default.MenuBook)
                            StatPill("$xp XP",Icons.Default.Bolt)
                        }
                        Button(onClick=onLearn,colors=ButtonDefaults.buttonColors(containerColor=Gold,contentColor=Deep),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Text("Continua a imparare",fontWeight=FontWeight.Bold);Spacer(Modifier.width(8.dp));Icon(Icons.Default.ArrowForward,null)}
                    }
                }
            }
        }
        item {
            Text("Scegli come esercitarti",fontSize=19.sp,fontWeight=FontWeight.Bold,color=Forest)
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                ActionCard("Lezioni","Dialetti e parole",Icons.Default.MenuBook,Modifier.weight(1f),onLearn)
                ActionCard("Parla","Allenati a voce",Icons.Default.Mic,Modifier.weight(1f),onTalk)
            }
        }
        item {
            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                ActionCard("Ripasso","Rivedi le lezioni",Icons.Default.Replay,Modifier.weight(1f),onReview)
                ActionCard("Professore","In sviluppo",Icons.Default.School,Modifier.weight(1f),onProfessor)
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth().clickable(onClick=onSettings)){
                Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                    Box(Modifier.size(42.dp).background(Mint,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Default.Tune,null,tint=Forest)}
                    Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Il tuo mix linguistico",fontWeight=FontWeight.Bold,color=Forest);Text("Personalizza dialetti e percentuali",fontSize=12.sp,color=Muted)}
                    Icon(Icons.Default.ChevronRight,null,tint=Muted)
                }
            }
        }
    }
}
@Composable private fun StatPill(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector){
    Row(Modifier.clip(CircleShape).background(Color.White.copy(alpha=.12f)).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Gold,modifier=Modifier.size(15.dp));Spacer(Modifier.width(6.dp));Text(label,color=Color.White,fontSize=12.sp,fontWeight=FontWeight.SemiBold)}
}
@Composable private fun ActionCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier,onClick:()->Unit){
    Card(modifier.clickable(onClick=onClick),colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(20.dp)){
        Column(Modifier.fillMaxWidth().heightIn(min=116.dp).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Box(Modifier.size(42.dp).background(Mint,CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=Forest)}
            Text(title,color=Forest,fontWeight=FontWeight.Bold,fontSize=16.sp,lineHeight=20.sp,maxLines=1)
            Text(subtitle,color=Muted,fontSize=13.sp,lineHeight=18.sp,maxLines=2)
        }
    }
}
// Curriculum reale: ogni unità contiene espressioni nuove, non rotazioni delle lezioni iniziali.
private val additionalCurriculum = listOf(
    Lesson("eg_food","Al ristorante","EGIZIANO","egy",listOf(
        Phrase("عايز المنيو","ʿāyez el-menيو","Vorrei il menu"), Phrase("ممكن مية؟","momken mayya?","Posso avere dell'acqua?"),
        Phrase("الحساب لو سمحت","el-ḥesāb law samaḥt","Il conto, per favore"), Phrase("الأكل جميل","el-akl gamīl","Il cibo è buonissimo"))),
    Lesson("eg_market","Fare acquisti","EGIZIANO","egy",listOf(
        Phrase("بكام ده؟","bekām da?","Quanto costa questo?"), Phrase("غالي أوي","ghāli awi","È molto caro"),
        Phrase("ممكن تخفيض؟","momken takhfīḍ?","È possibile uno sconto?"), Phrase("هدفع كاش","h-adfaʿ kāsh","Pago in contanti"))),
    Lesson("eg_directions","Chiedere indicazioni","EGIZIANO","egy",listOf(
        Phrase("المحطة فين؟","el-maḥaṭṭa fēn?","Dov'è la stazione?"), Phrase("على طول","ʿala ṭūl","Sempre dritto"),
        Phrase("يمين ولا شمال؟","yamīn walla shemāl?","A destra o a sinistra?"), Phrase("بعيد من هنا؟","baʿīd men henā?","È lontano da qui?"))),
    Lesson("eg_daily","La giornata","EGIZIANO","egy",listOf(
        Phrase("بصحى الساعة سبعة","baṣḥa es-sāʿa sabʿa","Mi sveglio alle sette"), Phrase("بروح الشغل","barūḥ esh-shoghl","Vado al lavoro"),
        Phrase("برجع البيت بالليل","barġaʿ el-bēt bel-lēl","Torno a casa la sera"), Phrase("أنا تعبان النهارده","ana taʿbān ennahārda","Oggi sono stanco"))),
    Lesson("mor_food","Al ristorante","MAROCCHINO · DARIJA","mor",listOf(
        Phrase("بغيت المينيو","bghit l-menu","Vorrei il menu"), Phrase("عافاك جيب ليا الما","ʿafak jib lia l-ma","Per favore portami dell'acqua"),
        Phrase("الحساب عافاك","l-ḥساب ʿafak","Il conto, per favore"), Phrase("الماكلة بنينة","l-makla bnina","Il cibo è buono"))),
    Lesson("mor_market","Al mercato","MAROCCHINO · DARIJA","mor",listOf(
        Phrase("بشحال هادا؟","bshḥal hada?","Quanto costa questo?"), Phrase("غالي بزاف","ghali bzzaf","È molto caro"),
        Phrase("نقص ليا شوية","nqeṣ lia shwiya","Fammi un piccolo sconto"), Phrase("غادي ناخدو","ghadi nakhdo","Lo prendo"))),
    Lesson("mor_directions","In città","MAROCCHINO · DARIJA","mor",listOf(
        Phrase("فين الطوبيس؟","fin ṭṭobis?","Dov'è l'autobus?"), Phrase("دور على اليمين","dur ʿla limin","Gira a destra"),
        Phrase("سير نيشان","sir nishan","Vai dritto"), Phrase("واش قريب؟","wash qrib?","È vicino?"))),
    Lesson("mor_daily","La routine","MAROCCHINO · DARIJA","mor",listOf(
        Phrase("كنفيق مع السبعة","kanfiq mʿa s-sebʿa","Mi sveglio alle sette"), Phrase("كنمشي للخدمة","kanmshi l-khedma","Vado al lavoro"),
        Phrase("كنرجع للعشية","kanrjeʿ l-ʿshiya","Ritorno nel pomeriggio"), Phrase("اليوم عيان","lyum ʿyan","Oggi sono stanco"))),
    Lesson("alg_food","Al ristorante","ALGERINO · DARIJA","alg",listOf(
        Phrase("عطيني القائمة","ʿṭini l-qayma","Dammi il menu"), Phrase("نحب ماء من فضلك","nḥeb ma men faḍlek","Vorrei dell'acqua, per favore"),
        Phrase("الحساب من فضلك","el-ḥsab men faḍlek","Il conto, per favore"), Phrase("الماكلة بنينة","el-makla bnina","Il cibo è buono"))),
    Lesson("alg_market","Comprare qualcosa","ALGERINO · DARIJA","alg",listOf(
        Phrase("بشحال هذا؟","bshḥal hada?","Quanto costa questo?"), Phrase("غالي بزاف","ghali bzzaf","È molto caro"),
        Phrase("ديرلي تخفيض","dirli takhfiḍ","Fammi uno sconto"), Phrase("نزيد نشوف","nzid nshuf","Continuo a guardare"))),
    Lesson("alg_directions","Orientarsi","ALGERINO · DARIJA","alg",listOf(
        Phrase("وين راهي المحطة؟","win rahi l-maḥaṭṭa?","Dov'è la stazione?"), Phrase("روح نيشان","ruḥ nishan","Vai dritto"),
        Phrase("على اليمين","ʿla lymin","A destra"), Phrase("بعيد شوية؟","bʿid shwiya?","È un po' lontano?"))),
    Lesson("alg_daily","La vita quotidiana","ALGERINO · DARIJA","alg",listOf(
        Phrase("نوض مع الستة","nuḍ mʿa s-setta","Mi alzo alle sei"), Phrase("نروح للخدمة","nruḥ l-khedma","Vado al lavoro"),
        Phrase("نرجع مع العشية","nrjeʿ mʿa l-ʿshiya","Rientro nel pomeriggio"), Phrase("راني مقلق اليوم","rani mqelleq lyum","Oggi sono preoccupato"))),
    Lesson("tn_food","Al ristorante","TUNISINO · DERJA","tun",listOf(
        Phrase("نحب المينيو","nḥeb el-menu","Vorrei il menu"), Phrase("ماء من فضلك","ma men faḍlek","Acqua, per favore"),
        Phrase("الحساب لو سمحت","el-ḥseb law samaḥt","Il conto, per favore"), Phrase("البنة هايلة","el-benna hayla","È buonissimo"))),
    Lesson("tn_market","Fare la spesa","TUNISINO · DERJA","tun",listOf(
        Phrase("بقداش هذا؟","bqaddech hedha?","Quanto costa questo?"), Phrase("غالي برشة","ghali barsha","È molto caro"),
        Phrase("تنجم تنقصلي؟","tnajjem tn aqqesli?","Puoi farmi uno sconto?"), Phrase("باش ناخذ هذا","bash nakhou hedha","Prendo questo"))),
    Lesson("tn_directions","Chiedere la strada","TUNISINO · DERJA","tun",listOf(
        Phrase("وين المحطة؟","win el-maḥaṭṭa?","Dov'è la stazione?"), Phrase("دغري","doghri","Dritto"),
        Phrase("على اليسار","ʿal yssar","A sinistra"), Phrase("قريب من غادي؟","qrib men ghadi?","È vicino da lì?"))),
    Lesson("tn_daily","La giornata","TUNISINO · DERJA","tun",listOf(
        Phrase("نفيق مع السبعة","nfiq mʿa s-sebʿa","Mi sveglio alle sette"), Phrase("نمشي للخدمة","nimshi lel-khedma","Vado al lavoro"),
        Phrase("نروح للعشية","nrouḥ lel-ʿshiya","Torno nel pomeriggio"), Phrase("اليوم تعبان","el-yum taʿban","Oggi sono stanco")))
)

private fun createEndlessLessons(base:List<Lesson>, count:Int):List<Lesson> {
    if(base.isEmpty() || count<=0) return emptyList()
    val activeKeys=base.map { it.key }.toSet()
    return additionalCurriculum.filter { it.key in activeKeys }.take(count)
}

@Composable private fun LearnScreen(items:List<Lesson>,mix:Mix,done:Set<String>,onSelect:(String)->Unit){
    var extraCount by rememberSaveable { mutableIntStateOf(12) }
    val endless = remember(items, extraCount) { createEndlessLessons(items, extraCount) }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item { Header("Percorso di studio","Impara, a modo tuo.","Le lezioni seguono il mix di dialetti che hai scelto.") }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(18.dp)){
                Column(Modifier.padding(16.dp)){Text("IL TUO MIX",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("Egiziano ${mix.egy}%  ·  Marocco ${mix.mor}%  ·  Algeria ${mix.alg}%  ·  Tunisia ${mix.tun}%  ·  Standard ${mix.msa}%",color=Color.White,fontSize=13.sp,lineHeight=20.sp)}
            }
        }
        if(items.isEmpty()) item { Text("Imposta le percentuali dei dialetti nel Profilo per iniziare.",color=Muted) }
        val allLessons=items+endless
        val completedCount=allLessons.count{done.contains(it.id)}
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("IL TUO AVANZAMENTO",color=Teal,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
                            Text("$completedCount di ${allLessons.size} lezioni completate",color=Forest,fontWeight=FontWeight.Bold,fontSize=16.sp)
                        }
                        Text("${if(allLessons.isEmpty()) 0 else (completedCount*100/allLessons.size)}%",color=Teal,fontWeight=FontWeight.ExtraBold,fontSize=20.sp)
                    }
                    LinearProgressIndicator(progress={if(allLessons.isEmpty()) 0f else completedCount.toFloat()/allLessons.size},modifier=Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),color=Teal,trackColor=Mint)
                    Text("Le lezioni completate restano disponibili per ripassare.",color=Muted,fontSize=12.sp)
                }
            }
        }
        item { Text("PERCORSO CONTINUO",color=Teal,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.3.sp) }
        items(items){ l -> LessonRow(l,done.contains(l.id),onSelect) }
        items(endless){ l -> LessonRow(l,done.contains(l.id),onSelect) }
        item {
            OutlinedButton(onClick={extraCount += 12},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)) {
                Icon(Icons.Default.Add,null); Spacer(Modifier.width(8.dp)); Text("Genera altre 12 lezioni")
            }
            Text("Le unità combinano progressivamente le frasi dei dialetti selezionati, alternando dialoghi, ascolto e ripasso. Il percorso continua senza un ultimo livello.",color=Muted,fontSize=12.sp,lineHeight=18.sp)
        }
    }
}

@Composable private fun LessonRow(l:Lesson,isCompleted:Boolean,onSelect:(String)->Unit) {
    Card(modifier=Modifier.fillMaxWidth().clickable{onSelect(l.id)},colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(18.dp)){
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(44.dp).background(Mint,CircleShape),contentAlignment=Alignment.Center){Text("ع",color=Forest,fontSize=24.sp,fontWeight=FontWeight.Bold)}
            Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(l.dialect,color=Teal,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text(l.title,color=Forest,fontSize=16.sp,fontWeight=FontWeight.Bold,lineHeight=21.sp,maxLines=2)}
            if(isCompleted) { Icon(Icons.Default.CheckCircle,null,tint=Teal,modifier=Modifier.size(27.dp)) } else { Icon(Icons.Default.PlayCircle,null,tint=Teal,modifier=Modifier.size(28.dp)) }
        }
    }
}

private fun toggleDialect(mix:Mix,key:String,enabled:Boolean):Mix {
    val current=when(key){"egy"->mix.egy;"mor"->mix.mor;"alg"->mix.alg;else->mix.tun}
    if(!enabled) return when(key){"egy"->mix.copy(egy=0);"mor"->mix.copy(mor=0);"alg"->mix.copy(alg=0);else->mix.copy(tun=0)}
    if(current>0) return mix
    var remaining=15
    var updated=mix
    val fromStandard=minOf(remaining,mix.msa)
    remaining-=fromStandard
    // If the standard-Arabic share is too small, take the difference from the largest active regional dialect.
    val donorKeys=listOf("egy","mor","alg","tun").filter{it!=key}.sortedByDescending {
        when(it){"egy"->updated.egy;"mor"->updated.mor;"alg"->updated.alg;else->updated.tun}
    }
    for(donor in donorKeys){
        if(remaining<=0) break
        val amount=minOf(remaining,when(donor){"egy"->updated.egy;"mor"->updated.mor;"alg"->updated.alg;else->updated.tun})
        updated=when(donor){"egy"->updated.copy(egy=updated.egy-amount);"mor"->updated.copy(mor=updated.mor-amount);"alg"->updated.copy(alg=updated.alg-amount);else->updated.copy(tun=updated.tun-amount)}
        remaining-=amount
    }
    val allocation=15-remaining
    return when(key){"egy"->updated.copy(egy=updated.egy+allocation);"mor"->updated.copy(mor=updated.mor+allocation);"alg"->updated.copy(alg=updated.alg+allocation);else->updated.copy(tun=updated.tun+allocation)}
}

@Composable private fun ProfileScreen(mix:Mix,onMixChange:(Mix)->Unit){
    val entries=listOf(
        Triple("egy","Arabo egiziano","La parlata del Cairo e dintorni"),
        Triple("mor","Marocchino · Darija","La darija del Marocco"),
        Triple("alg","Algerino · Darija","La darja dell'Algeria"),
        Triple("tun","Tunisino · Derja","La derja della Tunisia")
    )
    val values=mapOf("egy" to mix.egy,"mor" to mix.mor,"alg" to mix.alg,"tun" to mix.tun)
    val sumRegional=mix.egy+mix.mor+mix.alg+mix.tun
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item { Header("Impostazioni personali","Il tuo arabo.","Decidi tu quali varietà studiare e quanto spazio dare a ciascuna.") }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(20.dp)){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text("IL TUO MIX · TOTALE 100%",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
                    MixBar(mix)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Dialetti selezionati",color=Color.White.copy(alpha=.8f),fontSize=12.sp);Text("${sumRegional}% dialetti",color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp)}
                    Text("Arabo standard: ${mix.msa}% (quota rimanente)",color=Mint,fontSize=13.sp)
                }
            }
        }
        item { Text("SELEZIONA E REGOLA",color=Teal,fontWeight=FontWeight.Bold,fontSize=11.sp,letterSpacing=1.3.sp) }
        items(entries){ entry ->
            val key=entry.first
            val value=values[key]?:0
            val enabled=value>0
            val otherTotal=sumRegional-value
            val maxForThis=(100-otherTotal).coerceAtLeast(0)
            Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(horizontal=16.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){Text(entry.second,color=Forest,fontWeight=FontWeight.Bold,fontSize=16.sp);Text(entry.third,color=Muted,fontSize=12.sp)}
                        Switch(checked=enabled,onCheckedChange={checked->
                            onMixChange(toggleDialect(mix,key,checked))
                        })
                    }
                    if(enabled){
                        Row(verticalAlignment=Alignment.CenterVertically){Text("Percentuale",color=Muted,fontSize=12.sp);Spacer(Modifier.weight(1f));Text("$value%",color=Teal,fontWeight=FontWeight.ExtraBold,fontSize=19.sp)}
                        Slider(value=value.toFloat(),onValueChange={v->
                            val target=v.roundToInt().coerceIn(0,maxForThis)
                            val updated=when(key){"egy"->mix.copy(egy=target);"mor"->mix.copy(mor=target);"alg"->mix.copy(alg=target);else->mix.copy(tun=target)}
                            onMixChange(updated)
                        },valueRange=0f..100f,steps=99,colors=SliderDefaults.colors(thumbColor=Teal,activeTrackColor=Teal,inactiveTrackColor=Mint))
                        Text("Disponibile fino a $maxForThis%",color=Muted,fontSize=10.sp)
                    } else Text("Attiva per inserirlo nel tuo percorso.",color=Muted,fontSize=12.sp)
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Mint),shape=RoundedCornerShape(18.dp)){
                Row(Modifier.padding(16.dp),verticalAlignment=Alignment.Top){Icon(Icons.Default.Info,null,tint=Forest);Spacer(Modifier.width(10.dp));Text("La percentuale dell'arabo standard si aggiorna automaticamente: è la parte che manca per arrivare al 100%. Le tue scelte vengono salvate sul telefono.",color=Forest,fontSize=13.sp,lineHeight=19.sp)}
            }
        }
        item { Text("LISAN · Impara l'arabo che si parla davvero.",color=Muted,fontSize=11.sp,textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth().padding(vertical=10.dp)) }
    }
}
@Composable private fun MixBar(mix:Mix){
    Row(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),horizontalArrangement=Arrangement.spacedBy(2.dp)){
        val parts=listOf(mix.egy to Teal,mix.mor to Gold,mix.alg to Color(0xFF8BA7D9),mix.tun to Color(0xFFD58E78),mix.msa to Color.White.copy(alpha=.3f))
        parts.filter{it.first>0}.forEach{(v,c)->Box(Modifier.weight(v.toFloat()).fillMaxHeight().background(c))}
    }
}
private fun localeForDialect(key:String):Locale = when(key) {
    "egy" -> Locale.forLanguageTag("ar-EG")
    "mor" -> Locale.forLanguageTag("ar-MA")
    "alg" -> Locale.forLanguageTag("ar-DZ")
    "tun" -> Locale.forLanguageTag("ar-TN")
    else -> Locale.forLanguageTag("ar")
}

@Composable private fun LessonScreen(lesson:Lesson,alreadyCompleted:Boolean,onBack:()->Unit,onComplete:()->Unit){
    val tts=rememberTts(lesson.key)
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item { Text("‹  INDIETRO",color=Teal,fontWeight=FontWeight.Bold,modifier=Modifier.clickable(onClick=onBack));Spacer(Modifier.height(8.dp));Text(lesson.dialect,color=Teal,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.4.sp);Text(lesson.title,color=Forest,fontSize=29.sp,fontWeight=FontWeight.ExtraBold) }
        items(lesson.phrases){ phrase ->
            Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){Text("ESPRESSIONE",color=Muted,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Spacer(Modifier.weight(1f));IconButton(onClick={tts?.speak(phrase.ar,TextToSpeech.QUEUE_FLUSH,null,"phrase")}){Icon(Icons.Default.VolumeUp,"Ascolta",tint=Teal)}}
                    Text(phrase.ar,color=Forest,fontSize=34.sp,lineHeight=44.sp,textAlign=TextAlign.End,modifier=Modifier.fillMaxWidth())
                    Text(phrase.latin,color=Teal,fontWeight=FontWeight.SemiBold,fontSize=16.sp,lineHeight=22.sp)
                    Text(phrase.it,color=Forest,fontSize=16.sp,lineHeight=23.sp)
                }
            }
        }
        item { if(alreadyCompleted) Text("✓  LEZIONE GIÀ COMPLETATA",color=Teal,fontWeight=FontWeight.Bold,fontSize=12.sp) }
        item { Button(onClick=onComplete,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text(if(alreadyCompleted) "Ripeti lezione" else "Segna come completata  ·  +20 XP")} }
    }
}
@Composable private fun ReviewScreen(count:Int,completed:List<Lesson>,onSelect:(String)->Unit){
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item { Header("I tuoi progressi","Ripasso","Rivedi le lezioni che hai già completato.") }
        item { Text("$count lezioni completate",color=Teal,fontWeight=FontWeight.Bold) }
        items(completed){lesson->Text("•  ${lesson.title} · ${lesson.dialect}",color=Forest,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable{onSelect(lesson.id)}.background(Paper).padding(14.dp))}
        if(completed.isEmpty()) item { Text("Completa la tua prima lezione: la ritroverai qui.",color=Muted) }
    }
}
@Composable private fun rememberTts(dialectKey:String):TextToSpeech?{
    val c=LocalContext.current
    var engine by remember(dialectKey){mutableStateOf<TextToSpeech?>(null)}
    DisposableEffect(c,dialectKey){
        val locale=localeForDialect(dialectKey)
        val holder=arrayOfNulls<TextToSpeech>(1)
        val t=TextToSpeech(c){status->
            if(status==TextToSpeech.SUCCESS){
                try {
                    val instance=holder[0] ?: return@TextToSpeech
                    val availability=instance.isLanguageAvailable(locale)
                    val chosen=if(availability>=TextToSpeech.LANG_AVAILABLE) locale else Locale.forLanguageTag("ar")
                    instance.language=chosen
                    instance.setSpeechRate(0.82f)
                    instance.setPitch(1.0f)
                    val matchingVoice=instance.voices?.filter { it.locale.language=="ar" &&
                        it.locale.country.equals(locale.country,true) }?.maxByOrNull { it.quality }
                    if(matchingVoice!=null) instance.voice=matchingVoice
                } catch(_:Exception) { }
            }
        }
        holder[0]=t
        engine=t
        onDispose{t.stop();t.shutdown();engine=null}
    }
    return engine
}

private data class ProfessorAnswer(val title:String,val body:String,val arabic:String="",val latin:String="")

private fun answerProfessor(question:String):ProfessorAnswer {
    val q=question.lowercase(Locale.ITALIAN).trim()
    return when {
        q.contains("izzay") || q.contains("إزيك") || q.contains("come stai") ->
            ProfessorAnswer("Come chiedere «come stai?»","In egiziano colloquiale puoi dire «izzayyak?» rivolgendoti a un uomo e «izzayyik?» a una donna. La forma e la vocale finale cambiano in base all'interlocutore.","إزيك؟","izzayyak? (a un uomo) · izzayyik? (a una donna)")
        q.contains("shukran") || q.contains("شكرا") || q.contains("grazie") ->
            ProfessorAnswer("Dire grazie","«Shukran» è una forma comune e comprensibile in arabo standard e in molti dialetti. In marocchino è molto comune anche «shukran bzzaf» per dire «grazie mille».","شكراً","shukran · shukran bzzaf")
        q.contains("dialett") || q.contains("differenza") || q.contains("marocch") ->
            ProfessorAnswer("I dialetti non sono intercambiabili","Egiziano, marocchino, algerino e tunisino condividono molte radici, ma pronuncia, lessico e alcune strutture cambiano. Per rispondere bene serve sapere quale varietà vuoi usare e in quale situazione.")
        q.contains("pronunc") || q.contains("vocal") || q.contains("letter") ->
            ProfessorAnswer("Un consiglio sulla pronuncia","La traslitterazione italiana è solo un aiuto: alcuni suoni arabi, come ع (ʿayn), ح (ḥ) e ق (q), non hanno un equivalente perfetto in italiano. Ascolta più volte un parlante nativo e ripeti lentamente. La voce del telefono può non riprodurre fedelmente un dialetto.")
        q.contains("salut") || q.contains("ciao") ->
            ProfessorAnswer("Un saluto utile","In arabo standard puoi usare «as-salāmu ʿalaykum». In marocchino colloquiale è comune anche «s-slam, labas?» per salutare e chiedere se va tutto bene.","السلام عليكم","as-salāmu ʿalaykum")
        q.contains("non cap") || q.contains("ripeti") ->
            ProfessorAnswer("Chiedere di ripetere","Una frase utile in più varietà è «puoi ripetere?». La forma esatta cambia da paese a paese: dimmi il dialetto e ti preparo la versione più adatta.")
        else -> ProfessorAnswer("Studiamo insieme","Ho ricevuto la tua domanda, ma questa versione di LISAN non è ancora collegata a un modello AI online. Per evitare di inventare una regola, non ti darò una risposta spacciandola per certa. Specifica il dialetto (egiziano, marocchino, algerino, tunisino o standard), oppure chiedimi di spiegare una delle frasi già presenti nelle lezioni.")
    }
}

@Composable private fun ProfessorScreen(){
    val context=LocalContext.current
    val scope= rememberCoroutineScope()
    val notes by context.store.data.map { it[PROFESSOR_NOTES] ?: emptySet() }.collectAsState(initial=emptySet())
    var question by rememberSaveable { mutableStateOf("") }
    var answer by remember { mutableStateOf<ProfessorAnswer?>(null) }
    var feedback by rememberSaveable { mutableStateOf("") }
    val suggestions=listOf("Qual è la differenza tra arabo egiziano e marocchino?","Come si pronuncia la lettera ع?","Come dico grazie in marocchino?")
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item { Header("Il tuo insegnante","Chiedi al professore.","Dubbi, grammatica, pronuncia e differenze tra dialetti.") }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(22.dp)){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){BrandMark(size=42);Spacer(Modifier.width(12.dp));Column{Text("LISAN · PROFESSORE",color=Gold,fontSize=11.sp,fontWeight=FontWeight.Bold);Text("Insegnante personale",color=Color.White,fontWeight=FontWeight.Bold,fontSize=17.sp)}}
                    Text("Sezione in sviluppo. Puoi consultare le spiegazioni didattiche integrate; il collegamento a un vero assistente AI sarà attivato in una prossima versione.",color=Mint,fontSize=14.sp,lineHeight=21.sp)
                }
            }
        }
        item {
            Text("PROVA A CHIEDERE",color=Teal,fontWeight=FontWeight.Bold,fontSize=12.sp,letterSpacing=1.1.sp)
            suggestions.forEach { sample ->
                SuggestionChip(onClick={question=sample},label={Text(sample)},modifier=Modifier.padding(end=5.dp,bottom=5.dp))
            }
        }
        item {
            OutlinedTextField(value=question,onValueChange={question=it},modifier=Modifier.fillMaxWidth(),label={Text("Scrivi la tua domanda")},placeholder={Text("Es. Perché questa parola si pronuncia così?")},minLines=2,maxLines=4,shape=RoundedCornerShape(16.dp))
            Button(onClick={answer=answerProfessor(question);feedback=""},enabled=question.isNotBlank(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){
                Icon(Icons.Default.Send,null);Spacer(Modifier.width(8.dp));Text("Chiedi al professore")
            }
        }
        answer?.let { a ->
            item {
                Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(20.dp)){
                    Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                        Text("SPIEGAZIONE",color=Teal,fontWeight=FontWeight.Bold,fontSize=11.sp,letterSpacing=1.sp)
                        Text(a.title,color=Forest,fontWeight=FontWeight.ExtraBold,fontSize=21.sp)
                        Text(a.body,color=Forest,fontSize=15.sp,lineHeight=22.sp)
                        if(a.arabic.isNotBlank()){
                            Text(a.arabic,color=Forest,fontSize=30.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.End)
                            Text(a.latin,color=Teal,fontWeight=FontWeight.SemiBold)
                        }
                        Text("Risposta dalla guida didattica integrata: non è una verifica in tempo reale con un docente madrelingua.",color=Muted,fontSize=12.sp,lineHeight=18.sp)
                        Divider(color=Mint)
                        Text("Questa spiegazione ti è stata utile?",color=Forest,fontWeight=FontWeight.Bold,fontSize=14.sp,lineHeight=20.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            OutlinedButton(onClick={feedback="utile";scope.launch{context.store.edit{p->p[PROFESSOR_NOTES]=(p[PROFESSOR_NOTES]?:emptySet())+"utile:"+question.take(100)}}}){Icon(Icons.Default.ThumbUp,null);Spacer(Modifier.width(5.dp));Text("Sì")}
                            OutlinedButton(onClick={feedback="da rivedere";scope.launch{context.store.edit{p->p[PROFESSOR_NOTES]=(p[PROFESSOR_NOTES]?:emptySet())+"rivedere:"+question.take(100)}}}){Icon(Icons.Default.Flag,null);Spacer(Modifier.width(5.dp));Text("Da rivedere")}
                        }
                        if(feedback.isNotBlank()) Text("Feedback salvato sul dispositivo. Aiuterà a tenere traccia delle spiegazioni da migliorare.",color=Teal,fontSize=12.sp)
                    }
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Mint),shape=RoundedCornerShape(18.dp)){
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    Text("IL PROFESSORE IMPARA CON TE",color=Forest,fontWeight=FontWeight.Bold,fontSize=13.sp)
                    Text(notes.size.toString()+" feedback salvati su questo telefono. Per ora sono una memoria locale: non addestrano automaticamente un modello AI.",color=Forest,fontSize=12.sp,lineHeight=18.sp)
                }
            }
        }
    }
}

@Composable private fun Speak(mix:Mix){
    val c=LocalContext.current
    var result by rememberSaveable{mutableStateOf("La frase riconosciuta apparirà qui.")}
    var status by rememberSaveable{mutableStateOf("Premi il microfono e pronuncia una frase.")}
    var dialect by rememberSaveable{mutableStateOf("egy")}
    val voice=rememberTts(dialect)
    val options=listOf("egy" to "Egiziano","mor" to "Marocchino","alg" to "Algerino","tun" to "Tunisino","msa" to "Standard").filter{(key,_)->when(key){"egy"->mix.egy>0;"mor"->mix.mor>0;"alg"->mix.alg>0;"tun"->mix.tun>0;else->mix.msa>0}}
    LaunchedEffect(options){if(options.none{it.first==dialect})dialect=options.firstOrNull()?.first?:"msa"}
    val start={
        if(!SpeechRecognizer.isRecognitionAvailable(c)){status="Riconoscimento non disponibile"}
        else {
            val recognizer=SpeechRecognizer.createSpeechRecognizer(c)
            recognizer.setRecognitionListener(object:RecognitionListener{
                override fun onReadyForSpeech(p:Bundle?){status="Ti ascolto…"}
                override fun onBeginningOfSpeech(){}
                override fun onRmsChanged(v:Float){}
                override fun onBufferReceived(b:ByteArray?){}
                override fun onEndOfSpeech(){}
                override fun onError(e:Int){status="Non ho riconosciuto la frase. Riprova.";recognizer.destroy()}
                override fun onResults(b:Bundle?){result=b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?:"Nessun testo riconosciuto";status="Riconoscimento completato";recognizer.destroy()}
                override fun onPartialResults(b:Bundle?){}
                override fun onEvent(e:Int,b:Bundle?){}
            })
            val locale=when(dialect){"mor"->"ar-MA";"alg"->"ar-DZ";"tun"->"ar-TN";"msa"->"ar";else->"ar-EG"}
            recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,locale)})
        }
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)start()else status="Permesso microfono negato"}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
        item { Header("Allenamento vocale","Dai voce alle parole.","Scegli la varietà e prova a parlare.") }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(20.dp)){
                Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Text("VARIETÀ DA ESERCITARE",color=Teal,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
                    options.forEach{(key,label)->FilterChip(selected=dialect==key,onClick={dialect=key},label={Text(label)},leadingIcon=if(dialect==key){{Icon(Icons.Default.Check,null)}}else null)}
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Forest),shape=RoundedCornerShape(24.dp)){
                Column(Modifier.fillMaxWidth().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text("جَرِّبْ أَنْ تَتَكَلَّمَ",color=Gold,fontSize=28.sp)
                    Text("Premi il microfono e parla in arabo",color=Color.White,fontSize=15.sp,textAlign=TextAlign.Center)
                    FilledIconButton(onClick={if(ContextCompat.checkSelfPermission(c,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)start()else permission.launch(Manifest.permission.RECORD_AUDIO)},modifier=Modifier.size(76.dp),colors=IconButtonDefaults.filledIconButtonColors(containerColor=Gold,contentColor=Deep)){Icon(Icons.Default.Mic,"Parla",modifier=Modifier.size(34.dp))}
                    Text(status,color=Mint,fontSize=13.sp,textAlign=TextAlign.Center)
                }
            }
        }
        item { Card(colors=CardDefaults.cardColors(containerColor=Paper),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("TRASCRIZIONE",color=Teal,fontWeight=FontWeight.Bold,fontSize=12.sp);Spacer(Modifier.weight(1f));IconButton(onClick={voice?.speak(result,TextToSpeech.QUEUE_FLUSH,null,"recognized")}){Icon(Icons.Default.VolumeUp,"Riascolta",tint=Teal)}};Text(result,color=Forest,fontSize=19.sp)}} }
        item { Text("Nota: la disponibilità del riconoscimento per i dialetti dipende dai servizi vocali installati sul telefono.",color=Muted,fontSize=12.sp,lineHeight=18.sp) }
    }
}
