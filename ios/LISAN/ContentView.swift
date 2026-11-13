import SwiftUI
import AVFoundation
import Speech

private enum LisanStyle {
    static let forest = Color(red: 18/255, green: 59/255, blue: 53/255)
    static let deep = Color(red: 11/255, green: 41/255, blue: 39/255)
    static let teal = Color(red: 36/255, green: 111/255, blue: 96/255)
    static let mint = Color(red: 221/255, green: 241/255, blue: 233/255)
    static let gold = Color(red: 230/255, green: 185/255, blue: 107/255)
    static let ivory = Color(red: 245/255, green: 244/255, blue: 238/255)
    static let paper = Color(red: 1, green: 254/255, blue: 250/255)
    static let muted = Color(red: 89/255, green: 107/255, blue: 99/255)
}

struct Phrase: Identifiable, Hashable {
    let id = UUID()
    let arabic: String
    let latin: String
    let italian: String
}
struct Lesson: Identifiable, Hashable {
    let id: String
    let title: String
    let dialect: String
    let phrases: [Phrase]
}
private let lessonData: [Lesson] = [
    Lesson(id:"eg1",title:"I primi saluti",dialect:"EGIZIANO",phrases:[
        Phrase(arabic:"إزيك؟",latin:"izzayyak? / izzayyik?",italian:"Come stai?"),
        Phrase(arabic:"أنا كويس",latin:"ana kwayyis",italian:"Sto bene (uomo)"),
        Phrase(arabic:"الحمد لله",latin:"el-ḥamdu lillāh",italian:"Grazie a Dio"),
        Phrase(arabic:"شكراً",latin:"shokran",italian:"Grazie")]),
    Lesson(id:"eg2",title:"Presentarsi",dialect:"EGIZIANO",phrases:[
        Phrase(arabic:"أنا اسمي ...",latin:"ana ismi ...",italian:"Mi chiamo ..."),
        Phrase(arabic:"إنت اسمك إيه؟",latin:"inta ismak eh?",italian:"Come ti chiami?"),
        Phrase(arabic:"أنا من إيطاليا",latin:"ana min iṭālya",italian:"Vengo dall'Italia")]),
    Lesson(id:"ma1",title:"Saluti marocchini",dialect:"MAROCCHINO · DARIJA",phrases:[
        Phrase(arabic:"السلام، لاباس؟",latin:"s-slam, labas?",italian:"Ciao, tutto bene?"),
        Phrase(arabic:"كنبغي نتعلم الدارجة",latin:"kanbghi ntʿellem ddarija",italian:"Voglio imparare la darija"),
        Phrase(arabic:"شكراً بزاف",latin:"shukran bzzaf",italian:"Grazie mille"),
        Phrase(arabic:"بسلامة",latin:"bslama",italian:"Arrivederci")]),
    Lesson(id:"ma2",title:"Le prime risposte",dialect:"MAROCCHINO · DARIJA",phrases:[
        Phrase(arabic:"لاباس، الحمد لله",latin:"labas, lḥamdullah",italian:"Tutto bene, grazie a Dio"),
        Phrase(arabic:"واخا",latin:"wakha",italian:"Va bene"),
        Phrase(arabic:"فين كاين؟",latin:"fin kayn?",italian:"Dov'è?")]),
    Lesson(id:"dz1",title:"Saluti algerini",dialect:"ALGERINO · DARIJA",phrases:[
        Phrase(arabic:"واش راك؟",latin:"wach rak? / wach raki?",italian:"Come stai?"),
        Phrase(arabic:"راني مليح",latin:"rani mlih",italian:"Sto bene"),
        Phrase(arabic:"صحيت",latin:"saḥḥit",italian:"Grazie"),
        Phrase(arabic:"بزاف",latin:"bzzaf",italian:"Molto / tanto")]),
    Lesson(id:"tn1",title:"Saluti tunisini",dialect:"TUNISINO · DERJA",phrases:[
        Phrase(arabic:"عسلامة، شنية أحوالك؟",latin:"ʿaslema, shnowa aḥwelek?",italian:"Ciao, come stai?"),
        Phrase(arabic:"لاباس",latin:"labes",italian:"Tutto bene"),
        Phrase(arabic:"يعطيك الصحة",latin:"yaʿtik eṣ-ṣaḥḥa",italian:"Grazie"),
        Phrase(arabic:"برشة",latin:"barsha",italian:"Molto / tanto")]),
    Lesson(id:"msa1",title:"Saluti standard",dialect:"ARABO STANDARD",phrases:[
        Phrase(arabic:"السلام عليكم",latin:"as-salāmu ʿalaykum",italian:"La pace sia con voi"),
        Phrase(arabic:"صباح الخير",latin:"ṣabāḥ al-khayr",italian:"Buongiorno"),
        Phrase(arabic:"مساء الخير",latin:"masāʾ al-khayr",italian:"Buonasera")])
] + additionalLessonData

private let additionalLessonData: [Lesson] = [
    Lesson(id:"eg_food",title:"Al ristorante",dialect:"EGIZIANO",phrases:[Phrase(arabic:"عايز المنيو",latin:"ʿāyez el-menu",italian:"Vorrei il menu"),Phrase(arabic:"ممكن مية؟",latin:"momken mayya?",italian:"Posso avere dell'acqua?"),Phrase(arabic:"الحساب لو سمحت",latin:"el-ḥesāb law samaḥt",italian:"Il conto, per favore"),Phrase(arabic:"الأكل جميل",latin:"el-akl gamīl",italian:"Il cibo è buonissimo")]),
    Lesson(id:"eg_market",title:"Fare acquisti",dialect:"EGIZIANO",phrases:[Phrase(arabic:"بكام ده؟",latin:"bekām da?",italian:"Quanto costa questo?"),Phrase(arabic:"غالي أوي",latin:"ghāli awi",italian:"È molto caro"),Phrase(arabic:"ممكن تخفيض؟",latin:"momken takhfīḍ?",italian:"È possibile uno sconto?"),Phrase(arabic:"هدفع كاش",latin:"h-adfaʿ kāsh",italian:"Pago in contanti")]),
    Lesson(id:"eg_directions",title:"Chiedere indicazioni",dialect:"EGIZIANO",phrases:[Phrase(arabic:"المحطة فين؟",latin:"el-maḥaṭṭa fēn?",italian:"Dov'è la stazione?"),Phrase(arabic:"على طول",latin:"ʿala ṭūl",italian:"Sempre dritto"),Phrase(arabic:"يمين ولا شمال؟",latin:"yamīn walla shemāl?",italian:"A destra o a sinistra?"),Phrase(arabic:"بعيد من هنا؟",latin:"baʿīd men henā?",italian:"È lontano da qui?")]),
    Lesson(id:"eg_daily",title:"La giornata",dialect:"EGIZIANO",phrases:[Phrase(arabic:"بصحى الساعة سبعة",latin:"baṣḥa es-sāʿa sabʿa",italian:"Mi sveglio alle sette"),Phrase(arabic:"بروح الشغل",latin:"barūḥ esh-shoghl",italian:"Vado al lavoro"),Phrase(arabic:"برجع البيت بالليل",latin:"barġaʿ el-bēt bel-lēl",italian:"Torno a casa la sera"),Phrase(arabic:"أنا تعبان النهارده",latin:"ana taʿbān ennahārda",italian:"Oggi sono stanco")]),
    Lesson(id:"mor_food",title:"Al ristorante",dialect:"MAROCCHINO · DARIJA",phrases:[Phrase(arabic:"بغيت المينيو",latin:"bghit l-menu",italian:"Vorrei il menu"),Phrase(arabic:"عافاك جيب ليا الما",latin:"ʿafak jib lia l-ma",italian:"Portami dell'acqua, per favore"),Phrase(arabic:"الحساب عافاك",latin:"l-ḥsab ʿafak",italian:"Il conto, per favore"),Phrase(arabic:"الماكلة بنينة",latin:"l-makla bnina",italian:"Il cibo è buono")]),
    Lesson(id:"mor_market",title:"Al mercato",dialect:"MAROCCHINO · DARIJA",phrases:[Phrase(arabic:"بشحال هادا؟",latin:"bshḥal hada?",italian:"Quanto costa questo?"),Phrase(arabic:"غالي بزاف",latin:"ghali bzzaf",italian:"È molto caro"),Phrase(arabic:"نقص ليا شوية",latin:"nqeṣ lia shwiya",italian:"Fammi un piccolo sconto"),Phrase(arabic:"غادي ناخدو",latin:"ghadi nakhdo",italian:"Lo prendo")]),
    Lesson(id:"mor_directions",title:"In città",dialect:"MAROCCHINO · DARIJA",phrases:[Phrase(arabic:"فين الطوبيس؟",latin:"fin ṭṭobis?",italian:"Dov'è l'autobus?"),Phrase(arabic:"دور على اليمين",latin:"dur ʿla limin",italian:"Gira a destra"),Phrase(arabic:"سير نيشان",latin:"sir nishan",italian:"Vai dritto"),Phrase(arabic:"واش قريب؟",latin:"wash qrib?",italian:"È vicino?")]),
    Lesson(id:"mor_daily",title:"La routine",dialect:"MAROCCHINO · DARIJA",phrases:[Phrase(arabic:"كنفيق مع السبعة",latin:"kanfiq mʿa s-sebʿa",italian:"Mi sveglio alle sette"),Phrase(arabic:"كنمشي للخدمة",latin:"kanmshi l-khedma",italian:"Vado al lavoro"),Phrase(arabic:"كنرجع للعشية",latin:"kanrjeʿ l-ʿshiya",italian:"Ritorno nel pomeriggio"),Phrase(arabic:"اليوم عيان",latin:"lyum ʿyan",italian:"Oggi sono stanco")]),
    Lesson(id:"alg_food",title:"Al ristorante",dialect:"ALGERINO · DARIJA",phrases:[Phrase(arabic:"عطيني القائمة",latin:"ʿṭini l-qayma",italian:"Dammi il menu"),Phrase(arabic:"نحب ماء من فضلك",latin:"nḥeb ma men faḍlek",italian:"Vorrei dell'acqua"),Phrase(arabic:"الحساب من فضلك",latin:"el-ḥsab men faḍlek",italian:"Il conto, per favore"),Phrase(arabic:"الماكلة بنينة",latin:"el-makla bnina",italian:"Il cibo è buono")]),
    Lesson(id:"alg_market",title:"Comprare qualcosa",dialect:"ALGERINO · DARIJA",phrases:[Phrase(arabic:"بشحال هذا؟",latin:"bshḥal hada?",italian:"Quanto costa questo?"),Phrase(arabic:"غالي بزاف",latin:"ghali bzzaf",italian:"È molto caro"),Phrase(arabic:"ديرلي تخفيض",latin:"dirli takhfiḍ",italian:"Fammi uno sconto"),Phrase(arabic:"نزيد نشوف",latin:"nzid nshuf",italian:"Continuo a guardare")]),
    Lesson(id:"alg_directions",title:"Orientarsi",dialect:"ALGERINO · DARIJA",phrases:[Phrase(arabic:"وين راهي المحطة؟",latin:"win rahi l-maḥaṭṭa?",italian:"Dov'è la stazione?"),Phrase(arabic:"روح نيشان",latin:"ruḥ nishan",italian:"Vai dritto"),Phrase(arabic:"على اليمين",latin:"ʿla lymin",italian:"A destra"),Phrase(arabic:"بعيد شوية؟",latin:"bʿid shwiya?",italian:"È un po' lontano?")]),
    Lesson(id:"alg_daily",title:"La vita quotidiana",dialect:"ALGERINO · DARIJA",phrases:[Phrase(arabic:"نوض مع الستة",latin:"nuḍ mʿa s-setta",italian:"Mi alzo alle sei"),Phrase(arabic:"نروح للخدمة",latin:"nruḥ l-khedma",italian:"Vado al lavoro"),Phrase(arabic:"نرجع مع العشية",latin:"nrjeʿ mʿa l-ʿshiya",italian:"Rientro nel pomeriggio"),Phrase(arabic:"راني مقلق اليوم",latin:"rani mqelleq lyum",italian:"Oggi sono preoccupato")]),
    Lesson(id:"tn_food",title:"Al ristorante",dialect:"TUNISINO · DERJA",phrases:[Phrase(arabic:"نحب المينيو",latin:"nḥeb el-menu",italian:"Vorrei il menu"),Phrase(arabic:"ماء من فضلك",latin:"ma men faḍlek",italian:"Acqua, per favore"),Phrase(arabic:"الحساب لو سمحت",latin:"el-ḥseb law samaḥt",italian:"Il conto, per favore"),Phrase(arabic:"البنة هايلة",latin:"el-benna hayla",italian:"È buonissimo")]),
    Lesson(id:"tn_market",title:"Fare la spesa",dialect:"TUNISINO · DERJA",phrases:[Phrase(arabic:"بقداش هذا؟",latin:"bqaddech hedha?",italian:"Quanto costa questo?"),Phrase(arabic:"غالي برشة",latin:"ghali barsha",italian:"È molto caro"),Phrase(arabic:"تنجم تنقصلي؟",latin:"tnajjem tn aqqesli?",italian:"Puoi farmi uno sconto?"),Phrase(arabic:"باش ناخذ هذا",latin:"bash nakhou hedha",italian:"Prendo questo")]),
    Lesson(id:"tn_directions",title:"Chiedere la strada",dialect:"TUNISINO · DERJA",phrases:[Phrase(arabic:"وين المحطة؟",latin:"win el-maḥaṭṭa?",italian:"Dov'è la stazione?"),Phrase(arabic:"دغري",latin:"doghri",italian:"Dritto"),Phrase(arabic:"على اليسار",latin:"ʿal yssar",italian:"A sinistra"),Phrase(arabic:"قريب من غادي؟",latin:"qrib men ghadi?",italian:"È vicino da lì?")]),
    Lesson(id:"tn_daily",title:"La giornata",dialect:"TUNISINO · DERJA",phrases:[Phrase(arabic:"نفيق مع السبعة",latin:"nfiq mʿa s-sebʿa",italian:"Mi sveglio alle sette"),Phrase(arabic:"نمشي للخدمة",latin:"nimshi lel-khedma",italian:"Vado al lavoro"),Phrase(arabic:"نروح للعشية",latin:"nrouḥ lel-ʿshiya",italian:"Torno nel pomeriggio"),Phrase(arabic:"اليوم تعبان",latin:"el-yum taʿban",italian:"Oggi sono stanco")])
]

@main
struct LISANApp: App {
    var body: some Scene {
        WindowGroup { RootView().preferredColorScheme(.light) }
    }
}

struct RootView: View {
    @AppStorage("lisan.xp") private var xp = 0
    @AppStorage("lisan.completed") private var completedRaw = ""
    @State private var selectedTab = 0
    @State private var activeLesson: Lesson?
    private var completed: Set<String> { Set(completedRaw.split(separator: ",").map(String.init)) }

    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView(xp: xp, completed: completed.count,
                     learn: { selectedTab = 1 }, voice: { selectedTab = 2 },
                     review: { selectedTab = 1 }, professor: { selectedTab = 3 })
                .tabItem { Label("Home", systemImage: "house.fill") }.tag(0)
            LearnView(completed: completed) { activeLesson = $0 }
                .tabItem { Label("Impara", systemImage: "book.closed.fill") }.tag(1)
            VoiceView()
                .tabItem { Label("Voce", systemImage: "mic.fill") }.tag(2)
            ProfessorView()
                .tabItem { Label("Prof.", systemImage: "graduationcap.fill") }.tag(3)
            ProfileView(xp: xp, completed: completed.count)
                .tabItem { Label("Profilo", systemImage: "slider.horizontal.3") }.tag(4)
        }
        .tint(LisanStyle.teal)
        .background(LisanStyle.ivory)
        .sheet(item: $activeLesson) { lesson in
            LessonDetailView(lesson: lesson, isCompleted: completed.contains(lesson.id)) {
                if !completed.contains(lesson.id) {
                    var next = completed
                    next.insert(lesson.id)
                    completedRaw = next.sorted().joined(separator: ",")
                    xp += 20
                }
                activeLesson = nil
            }
        }
    }
}

struct BrandMark: View {
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 16).fill(LinearGradient(colors:[LisanStyle.teal,LisanStyle.forest],startPoint:.topLeading,endPoint:.bottomTrailing))
            Text("ل").font(.system(size:34,weight:.bold,design:.serif)).foregroundStyle(LisanStyle.gold)
        }.frame(width:48,height:48)
    }
}
struct ScreenHeader: View {
    let eyebrow: String
    let title: String
    var subtitle: String? = nil
    var body: some View {
        HStack(alignment:.center,spacing:12) {
            VStack(alignment:.leading,spacing:5) {
                Text(eyebrow.uppercased()).font(.system(size:11,weight:.bold)).tracking(1.2).foregroundStyle(LisanStyle.teal)
                Text(title).font(.system(size:28,weight:.heavy,design:.rounded)).foregroundStyle(LisanStyle.forest).fixedSize(horizontal:false,vertical:true)
                if let subtitle { Text(subtitle).font(.system(size:14)).foregroundStyle(LisanStyle.muted).fixedSize(horizontal:false,vertical:true) }
            }
            Spacer(minLength:4)
            BrandMark()
        }
        .padding(.bottom,8)
    }
}
struct SurfaceCard<Content: View>: View {
    @ViewBuilder var content: Content
    var body: some View {
        content.padding(18).frame(maxWidth:.infinity,alignment:.leading)
            .background(LisanStyle.paper,in:RoundedRectangle(cornerRadius:22,style:.continuous))
            .overlay(RoundedRectangle(cornerRadius:22).stroke(LisanStyle.forest.opacity(0.06),lineWidth:1))
    }
}
struct HomeView: View {
    let xp:Int; let completed:Int
    let learn:()->Void; let voice:()->Void; let review:()->Void; let professor:()->Void
    var body: some View {
        ScrollView {
            VStack(alignment:.leading,spacing:20) {
                ScreenHeader(eyebrow:"Benvenuto in LISAN",title:"Marḥaba.",subtitle:"L'arabo, con la voce e le parole di ogni giorno.")
                VStack(alignment:.leading,spacing:16) {
                    HStack { Text("IL TUO PERCORSO").font(.system(size:11,weight:.bold)).tracking(1.4).foregroundStyle(LisanStyle.gold); Spacer(); Text("LIVELLO A0").font(.system(size:11,weight:.semibold)).foregroundStyle(LisanStyle.mint) }
                    Text("Una lingua viva.\nQuattro voci da scoprire.").font(.system(size:26,weight:.bold,design:.rounded)).foregroundStyle(.white)
                    HStack(spacing:9) {
                        statPill("\(completed) lezioni",icon:"book.closed")
                        statPill("\(xp) XP",icon:"bolt.fill")
                    }
                    Button(action:learn) { HStack { Text("Continua a imparare"); Spacer(); Image(systemName:"arrow.right") }.fontWeight(.bold).foregroundStyle(LisanStyle.deep).padding(16).frame(maxWidth:.infinity).background(LisanStyle.gold,in:RoundedRectangle(cornerRadius:15)) }
                }
                .padding(22).background(LinearGradient(colors:[LisanStyle.forest,LisanStyle.deep],startPoint:.topLeading,endPoint:.bottomTrailing),in:RoundedRectangle(cornerRadius:28))
                Text("Scegli come esercitarti").font(.system(size:19,weight:.bold)).foregroundStyle(LisanStyle.forest)
                LazyVGrid(columns:[GridItem(.flexible()),GridItem(.flexible())],spacing:12) {
                    actionCard("Lezioni","Dialetti e parole","book.closed.fill",learn)
                    actionCard("Parla","Allenati a voce","mic.fill",voice)
                    actionCard("Ripasso","Rivedi le lezioni","arrow.counterclockwise",review)
                    actionCard("Professore","Guida allo studio","graduationcap.fill",professor)
                }
            }.padding(20)
        }.background(LisanStyle.ivory).scrollIndicators(.hidden)
    }
    private func statPill(_ text:String,icon:String)->some View {
        Label(text,systemImage:icon).font(.system(size:12,weight:.semibold)).foregroundStyle(.white).padding(.horizontal,12).padding(.vertical,9).background(.white.opacity(0.13),in:Capsule())
    }
    private func actionCard(_ title:String,_ subtitle:String,_ icon:String,_ action:@escaping()->Void)->some View {
        Button(action:action) {
            VStack(alignment:.leading,spacing:9) {
                Image(systemName:icon).font(.system(size:19,weight:.semibold)).foregroundStyle(LisanStyle.forest).frame(width:42,height:42).background(LisanStyle.mint,in:Circle())
                Text(title).font(.system(size:16,weight:.bold)).foregroundStyle(LisanStyle.forest)
                Text(subtitle).font(.system(size:12)).foregroundStyle(LisanStyle.muted).multilineTextAlignment(.leading)
            }.frame(maxWidth:.infinity,minHeight:112,alignment:.leading).padding(15).background(LisanStyle.paper,in:RoundedRectangle(cornerRadius:20))
        }.buttonStyle(.plain)
    }
}
struct LearnView: View {
    let completed:Set<String>; let onSelect:(Lesson)->Void
    var body: some View {
        ScrollView {
            VStack(alignment:.leading,spacing:15) {
                ScreenHeader(eyebrow:"Percorso di studio",title:"Impara, a modo tuo.",subtitle:"Le basi dei dialetti e dell'arabo standard.")
                SurfaceCard {
                    VStack(alignment:.leading,spacing:8) {
                        Text("IL TUO AVANZAMENTO").font(.system(size:11,weight:.bold)).tracking(1).foregroundStyle(LisanStyle.teal)
                        HStack { Text("\(completed.count) di \(lessonData.count) lezioni completate").font(.system(size:15,weight:.bold)).foregroundStyle(LisanStyle.forest); Spacer(); Text("\(Int(Double(completed.count)/Double(lessonData.count)*100))%").font(.headline.bold()).foregroundStyle(LisanStyle.teal) }
                        ProgressView(value:Double(completed.count),total:Double(lessonData.count)).tint(LisanStyle.teal)
                    }
                }
                Text("LEZIONI").font(.system(size:11,weight:.bold)).tracking(1.2).foregroundStyle(LisanStyle.teal)
                ForEach(lessonData) { lesson in
                    Button { onSelect(lesson) } label: {
                        HStack(spacing:13) {
                            Image(systemName:completed.contains(lesson.id) ? "checkmark.circle.fill":"book.closed.fill").font(.title3).foregroundStyle(completed.contains(lesson.id) ? LisanStyle.teal:LisanStyle.gold).frame(width:44,height:44).background(LisanStyle.mint,in:Circle())
                            VStack(alignment:.leading,spacing:4) {
                                Text(lesson.title).font(.system(size:16,weight:.bold)).foregroundStyle(LisanStyle.forest)
                                Text(lesson.dialect).font(.system(size:10,weight:.bold)).tracking(0.8).foregroundStyle(LisanStyle.muted)
                                Text("\(lesson.phrases.count) espressioni").font(.system(size:12)).foregroundStyle(LisanStyle.muted)
                            }
                            Spacer()
                            Image(systemName:"chevron.right").foregroundStyle(LisanStyle.muted)
                        }.padding(14).background(LisanStyle.paper,in:RoundedRectangle(cornerRadius:18))
                    }.buttonStyle(.plain)
                }
            }.padding(20)
        }.background(LisanStyle.ivory).scrollIndicators(.hidden)
    }
}
struct LessonDetailView: View {
    let lesson:Lesson; let isCompleted:Bool; let onComplete:()->Void
    @Environment(\.dismiss) private var dismiss
    private let speech = AVSpeechSynthesizer()
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment:.leading,spacing:16) {
                    Text(lesson.dialect).font(.system(size:11,weight:.bold)).tracking(1.1).foregroundStyle(LisanStyle.teal)
                    Text(lesson.title).font(.system(size:29,weight:.heavy,design:.rounded)).foregroundStyle(LisanStyle.forest)
                    ForEach(lesson.phrases) { phrase in
                        SurfaceCard {
                            VStack(alignment:.leading,spacing:9) {
                                HStack { Spacer(); Text(phrase.arabic).font(.system(size:31,weight:.medium,design:.serif)).foregroundStyle(LisanStyle.forest).environment(\.layoutDirection,.rightToLeft); Spacer(); Button { speak(phrase.arabic) } label:{Image(systemName:"speaker.wave.2.fill").foregroundStyle(LisanStyle.teal)} }
                                Text(phrase.latin).font(.system(size:15,weight:.medium)).foregroundStyle(LisanStyle.teal)
                                Text(phrase.italian).font(.system(size:15)).foregroundStyle(LisanStyle.muted)
                            }
                        }
                    }
                    Button(action:onComplete) { Text(isCompleted ? "Lezione già completata":"Completa la lezione · +20 XP").fontWeight(.bold).frame(maxWidth:.infinity).padding(16).background(LisanStyle.forest,in:RoundedRectangle(cornerRadius:16)).foregroundStyle(.white) }.disabled(isCompleted).opacity(isCompleted ? 0.65:1)
                }.padding(20)
            }.background(LisanStyle.ivory).toolbar { ToolbarItem(placement:.topBarTrailing) { Button("Chiudi"){dismiss()} } }
        }
    }
    private func speak(_ text:String) {
        let utterance=AVSpeechUtterance(string:text); utterance.voice=AVSpeechSynthesisVoice(language:"ar")
        speech.speak(utterance)
    }
}
final class SpeechManager: NSObject, ObservableObject, SFSpeechRecognizerDelegate {
    @Published var transcript = ""
    @Published var listening = false
    @Published var status = "Tocca il microfono e pronuncia una frase in arabo."
    private let engine=AVAudioEngine()
    private let recognizer=SFSpeechRecognizer(locale:Locale(identifier:"ar")) 
    private var request:SFSpeechAudioBufferRecognitionRequest?
    private var task:SFSpeechRecognitionTask?
    func start() {
        SFSpeechRecognizer.requestAuthorization { [weak self] auth in
            DispatchQueue.main.async {
                guard let self else{return}
                guard auth == .authorized else { self.status="Autorizza il riconoscimento vocale nelle Impostazioni."; return }
                self.begin()
            }
        }
    }
    private func begin() {
        task?.cancel(); task=nil
        let session=AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.record,mode:.measurement,options:.duckOthers)
            try session.setActive(true,options:.notifyOthersOnDeactivation)
            request=SFSpeechAudioBufferRecognitionRequest()
            guard let request else{return}
            request.shouldReportPartialResults=true
            let input=engine.inputNode
            input.removeTap(onBus:0)
            input.installTap(onBus:0,bufferSize:1024,format:input.outputFormat(forBus:0)){buffer,_ in request.append(buffer)}
            engine.prepare(); try engine.start()
            listening=true; status="Sto ascoltando…"
            task=recognizer?.recognitionTask(with:request){[weak self] result,error in
                if let result { DispatchQueue.main.async { self?.transcript=result.bestTranscription.formattedString } }
                if error != nil { DispatchQueue.main.async { self?.stop() } }
            }
        } catch { status="Impossibile avviare il microfono. Riprova." }
    }
    func stop() {
        if engine.isRunning { engine.stop(); engine.inputNode.removeTap(onBus:0) }
        request?.endAudio(); task?.cancel(); listening=false
        status="Registrazione terminata."
    }
}
struct VoiceView: View {
    @StateObject private var speech=SpeechManager()
    private let synthesizer=AVSpeechSynthesizer()
    var body: some View {
        VStack(spacing:22) {
            ScreenHeader(eyebrow:"Allenamento orale",title:"Dai voce alle parole.",subtitle:"Ascolta la pronuncia e prova a ripetere.")
            Spacer()
            Image(systemName:speech.listening ? "waveform":"mic.circle.fill").font(.system(size:76)).foregroundStyle(LisanStyle.teal).padding(28).background(LisanStyle.mint,in:Circle())
            Text(speech.status).font(.system(size:15)).foregroundStyle(LisanStyle.muted).multilineTextAlignment(.center)
            if !speech.transcript.isEmpty {
                Text(speech.transcript).font(.system(size:24,weight:.medium)).foregroundStyle(LisanStyle.forest).multilineTextAlignment(.center).padding()
            }
            Button { speech.listening ? speech.stop() : speech.start() } label: {
                Label(speech.listening ? "Ferma ascolto":"Inizia a parlare",systemImage:speech.listening ? "stop.fill":"mic.fill")
                    .fontWeight(.bold).frame(maxWidth:.infinity).padding(17).background(LisanStyle.forest,in:RoundedRectangle(cornerRadius:16)).foregroundStyle(.white)
            }
            Button { let u=AVSpeechUtterance(string:"السلام عليكم");u.voice=AVSpeechSynthesisVoice(language:"ar");synthesizer.speak(u) } label: {
                Label("Ascolta un esempio",systemImage:"speaker.wave.2.fill").fontWeight(.semibold).foregroundStyle(LisanStyle.teal).padding()
            }
            Spacer()
        }.padding(22).background(LisanStyle.ivory)
    }
}
struct ProfessorView: View {
    @State private var question=""
    @State private var answer="Ciao! Sono la guida di studio di LISAN. Posso aiutarti con alcune nozioni e frasi di base. Il professore AI conversazionale completo è ancora in sviluppo."
    private let examples=["Come si dice grazie in arabo?","Qual è la differenza tra arabo standard e dialetti?","Come saluto qualcuno in Marocco?"]
    var body: some View {
        ScrollView {
            VStack(alignment:.leading,spacing:17) {
                ScreenHeader(eyebrow:"Guida allo studio",title:"Il Professore.",subtitle:"Un aiuto per orientarti tra parole e dialetti.")
                SurfaceCard {
                    VStack(alignment:.leading,spacing:12) {
                        Label("GUIDA OFFLINE",systemImage:"graduationcap.fill").font(.system(size:11,weight:.bold)).tracking(1).foregroundStyle(LisanStyle.teal)
                        Text(answer).font(.system(size:15)).foregroundStyle(LisanStyle.forest).fixedSize(horizontal:false,vertical:true)
                    }
                }
                Text("PROVA A CHIEDERE").font(.system(size:11,weight:.bold)).tracking(1).foregroundStyle(LisanStyle.teal)
                ForEach(examples,id:\.self) { sample in
                    Button { question=sample; answer=reply(sample) } label: {
                        HStack { Text(sample).font(.system(size:14,weight:.medium)).foregroundStyle(LisanStyle.forest); Spacer(); Image(systemName:"arrow.up.left").foregroundStyle(LisanStyle.teal) }.padding(15).background(LisanStyle.paper,in:RoundedRectangle(cornerRadius:15))
                    }.buttonStyle(.plain)
                }
                HStack {
                    TextField("Scrivi una domanda…",text:$question).padding(13).background(LisanStyle.paper,in:RoundedRectangle(cornerRadius:14))
                    Button { answer=reply(question) } label: { Image(systemName:"paperplane.fill").foregroundStyle(.white).padding(15).background(LisanStyle.teal,in:RoundedRectangle(cornerRadius:14)) }
                }
                Text("Nota: le risposte sono esempi locali e non provengono da un'intelligenza artificiale online.").font(.system(size:12)).foregroundStyle(LisanStyle.muted)
            }.padding(20)
        }.background(LisanStyle.ivory)
    }
    private func reply(_ raw:String)->String {
        let q=raw.lowercased()
        if q.contains("grazie") { return "In molti contesti puoi dire شكراً (shukran). In Marocco si sente spesso anche شكراً بزاف (shukran bzzaf), cioè «grazie mille»." }
        if q.contains("marocc") || q.contains("maroc") { return "In Marocco si parla soprattutto darija. Un saluto informale è السلام، لاباس؟ (s-slam, labas?) — «Ciao, tutto bene?»." }
        if q.contains("standard") || q.contains("dialett") { return "L'arabo standard moderno è usato soprattutto in contesti formali, media e scrittura. Nella vita quotidiana si usano varietà regionali come egiziano, darija marocchina, algerino e tunisino." }
        if q.contains("ciao") || q.contains("salut") { return "Un saluto comune è السلام عليكم (as-salāmu ʿalaykum). In Egitto puoi sentire إزيك؟ (izzayyak?) per chiedere «Come stai?»." }
        return "Per ora posso rispondere a domande introduttive su saluti, ringraziamenti e differenze tra dialetti. Il professore AI completo richiederà un servizio online protetto."
    }
}
struct ProfileView: View {
    let xp:Int; let completed:Int
    @AppStorage("lisan.mix.egy") private var egyptian=75.0
    @AppStorage("lisan.mix.mor") private var moroccan=0.0
    @AppStorage("lisan.mix.alg") private var algerian=0.0
    @AppStorage("lisan.mix.tun") private var tunisian=0.0
    var body: some View {
        Form {
            Section { ScreenHeader(eyebrow:"Il tuo spazio",title:"Il tuo profilo.",subtitle:"Personalizza il percorso di LISAN.") }
            Section("I tuoi progressi") {
                LabeledContent("Lezioni completate",value:"\(completed)")
                LabeledContent("Punti esperienza",value:"\(xp) XP")
            }
            Section("Mix linguistico") {
                mixRow("Egiziano",value:$egyptian)
                mixRow("Marocchino",value:$moroccan)
                mixRow("Algerino",value:$algerian)
                mixRow("Tunisino",value:$tunisian)
                Text("Le lezioni standard sono sempre disponibili. Le percentuali sono preferenze personali, non devono sommare obbligatoriamente a 100.").font(.footnote).foregroundStyle(LisanStyle.muted)
            }
            Section { Text("LISAN · Impara l'arabo parlato, una frase alla volta.").font(.footnote).foregroundStyle(LisanStyle.muted) }
        }.scrollContentBackground(.hidden).background(LisanStyle.ivory)
    }
    private func mixRow(_ name:String,value:Binding<Double>)->some View {
        VStack(alignment:.leading) {
            HStack { Text(name); Spacer(); Text("\(Int(value.wrappedValue))%").foregroundStyle(LisanStyle.teal).fontWeight(.semibold) }
            Slider(value:value,in:0...100,step:5).tint(LisanStyle.teal)
        }.padding(.vertical,4)
    }
}
