mkdir -p dist
javapackager -deploy -native image -srcdir target -srcfiles imapnotesfx-0.0.1-SNAPSHOT.jar  -name ImapNotesFX  -outdir dist -appclass MainApp

