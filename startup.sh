#!/bin/sh
echo starting jackd in dummy mode
jackd -d dummy -r 44100 &


echo starting the repl
cd  sylt

#lein repl :start :host 0.0.0.0 :port 32768
#lein gorilla :port 8080 :ip 0.0.0.0 :nrepl-port 32768

#the gorilla repl and luminus are now conjoined
lein run

#waiting for jack and overtone to start, obviously has to be improved

# vlc startup was moved to lisp

# sleep 10
# echo starting vlc streamer in RTP mode
# cvlc jack://channels=2:ports=.* --sout "#transcode{vcodec=none,acodec=opus,ab=128,channels=2,samplerate=44100}:rtp{port=1234,sdp=rtsp://0.0.0.0:9999/test.sdp" 


#(load-file "/sylt/src/sylt/core.clj")
