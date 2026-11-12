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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale

private val Forest = Color(0xFF173D32)
private val Green = Color(0xFF2E8067)
private val Ivory = Color(0xFFF7F5EF)
private val Paper = Color(0xFFFFFEFA)
private val android.content.Context.store by preferencesDataStore("lisan_progress")
private val XP = intPreferencesKey("xp")
private val DONE = stringSetPreferencesKey("done")
private data class Phrase(val ar:String,val latin:String,val it:String)
private data class Lesson(val id:String,val title:String,val dialect:String,val phrases:List<Phrase>)
private val lessons = listOf(
 Lesson("eg1","I primi saluti","EGIZIANO",listOf(Phrase("إزيك؟","izzayyak? / izzayyik?","Come stai?"),Phrase("أنا كويس","ana kwayyis","Sto bene (uomo)"),Phrase("الحمد لله","el-ḥamdu lillāh","Grazie a Dio"),Phrase("شكراً","shokran","Grazie"))),
 Lesson("eg2","Presentarsi","EGIZIANO",listOf(Phrase("أنا اسمي ...","ana ismi ...","Mi chiamo ..."),Phrase("إنت اسمك إيه؟","inta ismak eh?","Come ti chiami?"),Phrase("أنا من إيطاليا","ana min iṭālya","Vengo dall'Italia"))),
 Lesson("msa1","Saluti standard","MSA",listOf(Phrase("السلام عليكم","as-salāmu ʿalaykum","La pace sia con voi"),Phrase("صباح الخير","ṣabāḥ al-khayr","Buongiorno"),Phrase("مساء الخير","masāʾ al-khayr","Buonasera")))
)
class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(247,245,239);setContent{ App() }}
}
@Composable private fun App(){
 val ctx= LocalContext.current
 val scope= rememberCoroutineScope()
 var page by rememberSaveable{mutableStateOf("Home")}
 var selected by rememberSaveable{mutableStateOf("")}
 val progress by ctx.store.data.map{(it[XP]?:0) to (it[DONE]?: emptySet())}.collectAsState(initial=0 to emptySet())
 val xp=progress.first; val done=progress.second
 val lesson=lessons.firstOrNull{it.id==selected}
 MaterialTheme(colorScheme= lightColorScheme(primary=Forest,secondary=Green,background=Ivory,surface=Paper)){
  Scaffold(containerColor=Ivory,bottomBar={if(lesson==null) NavigationBar(containerColor=Paper){listOf("Home","Impara","Conversa","Ripasso","Profilo").forEach{t->NavigationBarItem(selected=page==t,onClick={page=t},icon={Icon(when(t){"Home"->Icons.Default.Home;"Impara"->Icons.Default.MenuBook;"Conversa"->Icons.Default.Mic;"Ripasso"->Icons.Default.Replay;else->Icons.Default.Person},t)},label={Text(t,fontSize=10.sp)})}}}){
   padding->Column(Modifier.fillMaxSize().padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
    if(lesson!=null){
     Text("‹  INDIETRO",color=Green,modifier=Modifier.clickable{selected=""})
     Text(lesson.title,color=Forest,fontSize=28.sp,fontWeight=FontWeight.Bold)
     Text(lesson.dialect,color=Green,fontWeight=FontWeight.Bold)
     val tts=rememberTts()
     lesson.phrases.forEachIndexed{i,p->Card(colors=CardDefaults.cardColors(containerColor=Paper)){Column(Modifier.padding(15.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("0"+(i+1),color=Green);Spacer(Modifier.weight(1f));IconButton(onClick={tts?.speak(p.ar,TextToSpeech.QUEUE_FLUSH,null,"phrase"+i)}){Icon(Icons.Default.VolumeUp,"Ascolta")}};Text(p.ar,color=Forest,fontSize=30.sp,textAlign=TextAlign.End,modifier=Modifier.fillMaxWidth());Text(p.latin,color=Green,fontWeight=FontWeight.SemiBold);Text(p.it,color=Forest,fontSize=17.sp)}}}
     Button(onClick={scope.launch{ctx.store.edit{v->val d=(v[DONE]?: emptySet()).toMutableSet();if(d.add(lesson.id))v[XP]=(v[XP]?:0)+20;v[DONE]=d};selected="";page="Home"},modifier=Modifier.fillMaxWidth()){Text("Completa lezione · +20 XP")}
    }else when(page){
     "Home"->{Text("LISAN  /  IL TUO PERCORSO",color=Green,fontWeight=FontWeight.Bold);Text("Marḥaba.",color=Forest,fontSize=34.sp,fontWeight=FontWeight.Bold);Text("Il tuo arabo comincia da qui.");Card(colors=CardDefaults.cardColors(containerColor=Forest)){Column(Modifier.padding(20.dp)){Text("IL TUO PERCORSO · LIVELLO A0",color=Paper);Text("Le prime parole, le prime conversazioni.",color=Paper,fontSize=23.sp,fontWeight=FontWeight.Bold);Text(done.size.toString()+" lezioni · "+xp+" XP",color=Paper)}};Button(onClick={page="Impara"},modifier=Modifier.fillMaxWidth()){Text("Inizia a imparare")};Button(onClick={page="Conversa"},modifier=Modifier.fillMaxWidth()){Text("Allenamento vocale")}}
     "Impara"->{Text("Impara, un passo alla volta.",color=Forest,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Arabo egiziano 75% · Arabo standard 25%");lessons.forEach{l->Card(Modifier.fillMaxWidth().clickable{selected=l.id},colors=CardDefaults.cardColors(containerColor=Paper)){Column(Modifier.padding(16.dp)){Text(l.dialect,color=Green,fontWeight=FontWeight.Bold);Text(l.title,color=Forest,fontSize=18.sp)}}}}
     "Conversa"->Speak()
     "Ripasso"->{Text("Il tuo ripasso",color=Forest,fontSize=28.sp,fontWeight=FontWeight.Bold);Text(done.size.toString()+" lezioni completate");lessons.filter{done.contains(it.id)}.forEach{l->Text(l.title,color=Green,modifier=Modifier.clickable{selected=l.id})}}
     else->{Text("Il tuo profilo",color=Forest,fontSize=28.sp,fontWeight=FontWeight.Bold);Text("Studente LISAN · Principiante A0");Text(xp.toString()+" XP");Text("Arabo egiziano · 75%");Text("Arabo standard · 25%")}
    }
   }
  }
 }
}
@Composable private fun rememberTts():TextToSpeech?{
 val c=LocalContext.current;var engine by remember{mutableStateOf<TextToSpeech?>(null)}
 DisposableEffect(c){val t=TextToSpeech(c){s->if(s==TextToSpeech.SUCCESS){try{t.language=Locale("ar","EG")}catch(_:Exception){}}};engine=t;onDispose{t.stop();t.shutdown();engine=null}}
 return engine
}
@Composable private fun Speak(){
 val c=LocalContext.current;var result by rememberSaveable{mutableStateOf("La frase riconosciuta apparirà qui.")};var status by rememberSaveable{mutableStateOf("Premi il microfono e pronuncia إزيك؟")}
 val start={if(!SpeechRecognizer.isRecognitionAvailable(c)){status="Riconoscimento non disponibile"}else{val r=SpeechRecognizer.createSpeechRecognizer(c);r.setRecognitionListener(object:RecognitionListener{
 override fun onReadyForSpeech(p:Bundle?){status="Ti ascolto…"};override fun onBeginningOfSpeech(){};override fun onRmsChanged(v:Float){};override fun onBufferReceived(b:ByteArray?){};override fun onEndOfSpeech(){}
 override fun onError(e:Int){status="Non ho riconosciuto la frase. Riprova.";r.destroy()}
 override fun onResults(b:Bundle?){result=b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?: "Nessun testo riconosciuto";status="Riconoscimento completato";r.destroy()}
 override fun onPartialResults(b:Bundle?){};override fun onEvent(e:Int,b:Bundle?){}
 });r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ar-EG")})}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)start()else status="Permesso microfono negato"}
 Text("Dai voce alle parole.",color=Forest,fontSize=27.sp,fontWeight=FontWeight.Bold)
 Text("إزيك؟",color=Forest,fontSize=42.sp)
 Text("izzayyak? / izzayyik? · Come stai?")
 FilledIconButton(onClick={if(ContextCompat.checkSelfPermission(c,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)start()else permission.launch(Manifest.permission.RECORD_AUDIO)},modifier=Modifier.size(76.dp)){Icon(Icons.Default.Mic,"Parla")}
 Text(status);Text(result,color=Green,fontSize=20.sp)
 Text("La trascrizione non misura la qualità della pronuncia.",fontSize=12.sp)
}
