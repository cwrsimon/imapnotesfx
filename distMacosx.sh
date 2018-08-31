mkdir -p dist
/Library/Java/JavaVirtualMachines/jdk-10.0.2.jdk/Contents/Home/bin/javapackager -deploy -native image -srcdir target -srcfiles imapnotesfx-0.0.1-SNAPSHOT.jar  -name ImapNotesFX  -outdir dist -appclass MainApp

