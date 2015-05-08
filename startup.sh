#!/bin/sh
echo starting jackd in dummy mode
jackd  --silent -d dummy&
echo starting vlc streamer in RTP mode
# cvlc \
# jack://channels=2:ports=.* \
# --sout "#transcode{vcodec=none,acodec=opus,ab=128,channels=2,samplerate=44100}:rtp{port=1234,sdp=rtsp://0.0.0.0:9999/test.sdp" &

echo starting the repl
cd  insane-noises
lein repl

#(load-file "/insane-noises/src/insane_noises/core.clj")
