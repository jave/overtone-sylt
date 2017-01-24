;; * namespace declaration
;; the overtone live and core seems to clash, so you need to select one of them
;;(load-file "/home/joakim/overtone-sylt/sylt-docker/sylt/src/insane_noises/core.clj")

;; and a little sine wave
;; #+BEGIN_SRC clojure

(ns insane-noises.core
  (:require [overtone.api])
  (:use
   [overtone.live] ;; for the internal sc server
   ;;   [overtone.core]
   [overtone.inst synth piano drum]
   [overtone.examples.instruments space monotron]
   )
  (:require [me.raynes.conch :as sh])
  )


;;(overtone.core/boot-external-server)
(demo (sin-osc))
;; #+END_SRC
;; * some industrial soounding beats
;; i modified some of the dubstep in the tutorials to arrive at these nice harsh beats.

;; #+BEGIN_SRC clojure
(defsynth industrial [bpm 250 wobble 1 note 32 snare-vol 1 kick-vol 1 v 1]
  (let [trig (impulse:kr (/ bpm 120))
        freq (midicps note)
        swr (demand trig 0 (dseq [wobble] INF))
        sweep (lin-exp (lf-tri swr) -1 1 40 3000)
        wob (apply + (saw (* freq [0.99 1.01])))
        wob (lpf wob sweep)
        wob (* 0.8 (normalizer wob))
        wob (+ wob (bpf wob 1500 2))
        wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

        kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0
                                    (dseq [1 0 0 0
                                           1 0 0 0
                                           1 0 0 0
                                           1 0 0 0] INF)
                                    )) 0.7)
        bassenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0
                                    (dseq [1 0 1 0
                                           1 0 1 0
                                           1 0 1 0
                                           1 0 1 0] INF)
                                    )) 0.7)       

        kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
        kick (clip2 kick 1)

        bass (* (* bassenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
        bass (clip2 bass 1)

        snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
        snare (+ snare (bpf (* 4 snare) 2000))
        snare (clip2 snare 1)]

    (out 0    (* v (clip2 (+ wob bass (* kick-vol kick) (* snare-vol snare)) 1)))))

(defsynth industrial2 [bpm 250 wobble 1 note 32 snare-vol 1 kick-vol 1 v 1 wobble-vol 1]
  (let [trig (impulse:kr (/ bpm 120))
        freq (midicps note)
        swr (demand trig 0 (dseq [wobble] INF))
        sweep (lin-exp (lf-tri swr) -1 1 40 3000)
        wob (apply + (saw (* freq [0.99 1.01])))
        wob (lpf wob sweep)
        wob (* 0.8 (normalizer wob))
        wob (+ wob (bpf wob 1500 2))
        wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

        kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0
                                    (dseq [1 0 0 0
                                           1 0 0 0
                                           1 0 0 0
                                           1 0 0 0] INF)
                                    )) 0.7)
        bassenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0
                                    (dseq [1 0 1 0
                                           1 0 1 0
                                           1 0 1 0
                                           1 0 1 0] INF)
                                    )) 0.7)       

        kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
        kick (clip2 kick 1)

        bass (* (* bassenv 7) (lpf (saw 50)) 25)
        bass (clip2 bass 1)

        snare (* 3 (pink-noise) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
        snare (+ snare (bpf (* 4 snare) 2000))
        snare (clip2 snare 1)]

    (out 0    (* v (clip2 (+ (* wobble-vol wob)
                             bass
                             (* kick-vol kick) (* snare-vol snare)) 1)))))

(comment
  ;;Control the industrial synth with the following:
  (def d (industrial2))
  (ctl d :wobble 1)
  (ctl d :wobble-vol 1)
  (ctl d :kick-vol 1)
  (ctl d :note 40)
  (ctl d :bpm 250)
  (stop)
  )
;; #+END_SRC

;; * First, we'll define some percussive instruments

;; this high hat instrument takes a white noise generator and shapes
;; it with a percussive envelope

;; #+BEGIN_SRC clojure
(definst hat [volume 1.0]
  (let [src (white-noise)
        env (env-gen (perc 0.001 0.3) :action FREE)]
    (* volume 1 src env)))

(comment
  (hat)
  )
;; #+END_SRC

;; * some basic tutorial code

;;just a workaround because i didnt have stuff cached

;; #+BEGIN_SRC clojure
(defn frp [x]

                                        ;  "/home/joakim/.config/google-chrome-unstable/Default/Extensions/bepbmhgboaologfdajaanbcjmnhjmhfn/0.1.1.5023_0/audio/1_short_Open_16_16.wav"
  (freesound-path x)
  )
;; #+END_SRC

;; sampled kick drum
;; from http://www.freesound.org/people/opm/sounds/2086/
;; the overtone freesound API allows you to download freesounds samples
;; by id (2086 in this case)

;; this conflicted, and then it wasnt cached

;; #+BEGIN_SRC clojure
(def kick2086 (sample (frp 2086)))

(comment
  (kick)
  )
;; #+END_SRC

;; we can schedule beats for the future with the at macro:

;; #+BEGIN_SRC clojure
(comment
  (at (+ 1000 (now)) (kick))
  )
;; #+END_SRC

;; ...and chain multiple beats together with a do form:
;; #+BEGIN_SRC clojure
(comment
  (let
      [time (now)]
    (at (+    0 time) (kick) )
    (at (+  400 time) (hat)  )
    (at (+  800 time) (kick) )
    (at (+ 1200 time) (hat)  ))
  )
;; #+END_SRC
;; to repeat, we use the apply-at macro to schedule a recursive call
;; for the future
;; #+BEGIN_SRC clojure
(defn loop-beats [time]
  (at (+    0 time) (kick) )
  (at (+  400 time) (hat)  )
  (at (+  800 time) (kick) )
  (at (+ 1200 time) (hat)  )
  (apply-at (+ 1600 time) loop-beats (+ 1600 time) []))

(comment
  (loop-beats (now))
  )
;; #+END_SRC
;; rather than thinking in terms of milliseconds, it's useful to think
;; in terms of beats. We can create a metronome to help with this. A
;; metronome counts beats over time. Here's a metronome at 180 beats
;; per minute (bpm):
;; #+BEGIN_SRC clojure
(defonce metro (metronome 240))
;; #+END_SRC
;; we use it as follows:
;; #+BEGIN_SRC clojure
(metro) ; current beat number
(metro 3) ; timestamp of beat number 3
;; #+END_SRC
;; if we rewrite loop-beats using a metronome, it would look like
;; this:
;; #+BEGIN_SRC clojure
(defn metro-beats [m beat-num]
  (at (m (+ 0 beat-num)) (kick))
  (at (m (+ 1 beat-num)) (hat))
  (at (m (+ 2 beat-num)) (kick))
  (at (m (+ 3 beat-num)) (hat))
  (apply-at (m (+ 4 beat-num)) metro-beats m (+ 4 beat-num) [])
  )

(comment
  (metro-beats metro (metro))
  )
;; #+END_SRC
;; because we're using a metronome, we can change the speed:
;; #+BEGIN_SRC clojure
(comment
  (metro :bpm 180) ;slower
  (metro :bpm 300) ;faster
  )
;; #+END_SRC
;; a more complex rhythm
;; #+BEGIN_SRC clojure
(defn weak-hat []
  (hat 0.3))
;; #+END_SRC
;; phat beats
;; i just played around with the tut code a bit more to create a little beat
;; #+BEGIN_SRC clojure
(defn phat-beats [m beat-num]
  (at (m (+ 0 beat-num)) (kick) (weak-hat))
  (at (m (+ 1 beat-num)) (kick))
  (at (m (+ 2 beat-num)) (kick)       (hat))
  (at (m (+ 3 beat-num)) (kick) (weak-hat))
  (at (m (+ 4 beat-num)) (kick) (weak-hat))
  (at (m (+ 4.5 beat-num)) (kick))
  (at (m (+ 5 beat-num)) (kick))
  (at (m (+ 6 beat-num)) (kick) (hat) )
  (at (m (+ 7 beat-num)) (kick)       (weak-hat) )
  (apply-at (m (+ 8 beat-num)) phat-beats m (+ 8 beat-num) [])
  )

(comment
  (phat-beats metro (metro))
  )

(defn mytb303 []
  ;;this is dubious now
  ;;  (tb303 50 :wave 3 :amp 10  :cutoff 18)
  )

(defn mykick []
  (dance-kick 40)
  )
(defn mysnare []
  (snare 100)
  )

(defn myh1 []
  (closed-hat :low 1000 :hi 2000)
  )

(defn myh2 []
  (closed-hat :low 1000 :hi 2000)
  )

                                        ;(definst myblip []  (g-verb(blip 100 200)))

(definst myblip
  [note 60 amp 0.7 attack 0.001 release 2]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat 
                      (g-verb
                       (blip [freq note]))))
        audio (* amp env sig)]
    audio))

(defn myseq [note]
  ;;2 overpad for xtra phat(with echo and chorus)
  (overpad  :note (- note 10) :release 0.4 :dur 1)
  (overpad  :note (- note 10) :release 0.4 :dur 1)
  ;;bliptrack sometimes
  ;;(overpad  :note (+ note 15) :release 0.05 :dur 0.5)
                                        ;high blip seldom
  ;;(overpad  :note (+ note 32) :release 0.1 :dur 0.5)
                                        ;(simple-flute )
  ;; (g-verb (blip (mouse-y 24
  ;;         48) (mouse-x 1 100)) 200 8)
  ;;(myblip :note  (- note 00) :release 0.15)
  )
;; #+END_SRC

;; * simple beats
;; continues on plhat beats
;; #+BEGIN_SRC clojure
(defn simple-beats [m beat-num]
  (at (m (+ 0 beat-num))   (mytb303) (myh1) (mykick) (myseq 50)
      ;;(dream-inc)
      )
  (at (m (+ 0.5 beat-num)) (myh2) (myseq 55))
  (at (m (+ 1 beat-num))   (myh1) (mysnare)(myseq 50))
  (at (m (+ 1.5 beat-num)) (myh2) (myseq 55))  
  (at (m (+ 2 beat-num))   (myh1) (mykick)(myseq 55)(noise-snare))
  (at (m (+ 2.5 beat-num)) (myh2) (myseq 55))    
  (at (m (+ 3 beat-num))   (myh1) (mysnare)(myseq 50))
  (at (m (+ 3.5 beat-num)) (myh1) (myseq 55))    
  (at (m (+ 4 beat-num))   (myh1) (mykick)(myseq 55))
  (at (m (+ 4.5 beat-num)) (myh2) (myseq 55))    
  (at (m (+ 5 beat-num))   (myh1) (mysnare)(myseq 50))
  (at (m (+ 5.5 beat-num)) (myh2) (myseq 55))    
  (at (m (+ 6 beat-num))   (myh1) (mykick)(myseq 55)(noise-snare))
  (at (m (+ 6.5 beat-num)) (myh1) (myseq 55) )    
  (at (m (+ 7 beat-num))   (myh2) (mysnare)(myseq 5))
  (at (m (+ 7.5 beat-num)) (myh1) (myseq 55)(noise-snare))  
  
  (apply-at (m (+ 8 beat-num)) simple-beats m (+ 8 beat-num) [])
  )
;; #+END_SRC
;; psy beats
;; here i tried to get a psytrance feeling, but it wound up as something else. still trancey though.

;; #+BEGIN_SRC clojure
(defn psykick []
  (kick4 40 :decay 2 )
  (kick 50 :decay 2 )
  (dance-kick 40 :decay 0.25 )
  )
(defn psysnare []
  (noise-snare :decay 0.7 )
  )

(definst psybass
  [note 60 amp 0.7 attack 0.001 release 0.2 numharm 200]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat 
                      (g-verb
                       (blip [freq note ] :numharm numharm))))
        audio (* amp env sig)]
    audio))

(definst psybass2
  [note 60 amp 0.7 attack 0.001 release 0.2 numharm 200]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        f-env (+ freq (* 3 freq (env-gen (perc 0.012 (- release 0.1)))))
        bfreq (/ freq 2)
        sig   (apply +
                     (concat 
                      (g-verb
                       (blip [freq note ] :numharm numharm))))
        audio (* amp env sig)]
    audio))

(definst psybass3
  [note 60 amp 0.7 attack 0.001 release 0.2 numharm 200]
  (let [freq  (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        sig   (apply +
                     (concat 
                      
                      (bpf (saw [freq note ]) numharm )))
        sig2   (apply +
                      (concat 
                       
                       (bpf (saw [freq note ])  numharm )))

        audio (* amp env sig)
        audio2 (* amp env sig2)]
    [audio audio2]))


(defn psyh1 []
  (closed-hat 1 :low 1000 :hi 1500)
  )

(defn rand-int-range [a b]
  (+ a (rand-int (inc (- b a)))))

(defn psy-beats [m beat-num]
                                        ;(psybass m beat-num)
  (at (m (+ 0 beat-num))  ( psyh1) (psykick) (psybass 40 :numharm (rand-int-range 10 190)  )
      (psybass2 40 :numharm (rand-int-range 10 19)  )
      )
  (at (m (+ 1 beat-num))  ( psyh1) (myseq 40)(psybass 50 :numharm (rand-int-range 10 190)  )
      (psybass2 40 :numharm (rand-int-range 10 19)  ))
  (at (m (+ 2 beat-num))  ( psyh1) (myseq 40)(psybass 60 :numharm (rand-int-range 10 190) )
      (psybass2 40 :numharm (rand-int-range 10 19)  ))

  (at (m (+ 3 beat-num))  ( psyh1) (psykick) (psysnare) (psybass 40 :numharm (rand-int-range 10 190)  )
      (psybass2 40 :numharm (rand-int-range 10 19)  ))
  (at (m (+ 4 beat-num))  ( psyh1) (myseq 40)(psybass 50 :numharm (rand-int-range 10 190) )
      (psybass2 40 :numharm (rand-int-range 10 19)  ))
  (at (m (+ 5 beat-num))  ( psyh1) (myseq 40)(psybass 60 :numharm (rand-int-range 10 190))
      (psybass2 40 :numharm (rand-int-range 10 19)  ))

  (apply-at (m (+ 6 beat-num)) psy-beats m (+ 6 beat-num) [])
  )




(comment
  (metro :bpm 480)
  (psy-beats metro (metro))
  ;;  (psybass metro (metro))
  
  (inst-fx! overpad fx-echo)
  (inst-fx! overpad fx-chorus )
  (clear-fx overpad)
  
  (inst-fx! closed-hat fx-echo)
  (inst-fx! closed-hat fx-chorus)
  (clear-fx closed-hat)

  (apply (choose [(fn [] (inst-fx! psybass fx-echo))
                  (fn [] (inst-fx! psybass fx-chorus))
                  (fn [] (inst-fx! psybass fx-reverb))
                  (fn []
                    (clear-fx psybass)
                    )
                  ])
         nil)
  (inst-fx! psybass2 fx-distortion-tubescreamer)
  (clear-fx psybass2)
  (do
    (inst-fx! psybass fx-echo)
    (inst-fx! psybass fx-chorus)
    (inst-fx! psybass fx-reverb))
  (clear-fx psybass)

  
  (stop)
  )
;; #+END_SRC

;; * forest dream,

;; first i played with an old track called am i alive. this variation didnt turn out as much yet.

;; then i wrote an entirely new song around this beat that turned out rather good, called forest dream!

;; #+BEGIN_SRC clojure
(def dreamidx (agent 0))


(definst dream [bufnum 0]
  (play-buf :num-channels 1 :bufnum bufnum :rate 0.8)
  ;;(send dreamidx inc )
  ;;(if (< 10 @dreamidx) (send dreamidx inc ) (  ))

  )
(defn dream-inc [] (send dreamidx inc )(dream @dreamidx))
(def choir (sample (frp 46712)))
(def choir2 (sample (frp 65801)))
(def choir3 (load-sample (frp 65801)))
(def choir4 (load-sample (frp 46712)))
(definst choir2s []
  (* 64 (play-buf :num-channels 1 :bufnum choir3 :rate 1)))

(definst choir4s []
  (* 64 (play-buf :num-channels 2 :bufnum choir4 :rate 1)))

(comment
  (simple-beats metro (metro))
                                        ;(inst-fx! overpad fx-compressor)
                                        ;(inst-fx! overpad fx-sustainer)
                                        ;(inst-fx! overpad fx-freeverb)
  (def s1 (load-sample   "/home/joakim/roles/am_i_alive_all/am_i_alive/01.wav"))
  (def s2 (load-sample   "/home/joakim/roles/jave/music/am_i_alive_all/am_i_alive/02.wav"))

  ;;(map (fn [x] (load-sample (format   "/home/joakim/roles/jave/music/am_i_alive_all/am_i_alive/%s.wav" x))) '["01" "02"])
  (def dream-samples (for [i (range 1 40)] (load-sample (format   "/home/joakim/roles/jave/music/am_i_alive_all/am_i_alive/%02d.wav" i))))
  ;;(def dream-samples (load-samples    "/home/joakim/roles/jave/music/am_i_alive_all/am_i_alive/*.wav"))
  
                                        ;either
  (inst-fx! overpad fx-echo)
  (inst-fx! overpad fx-chorus )
                                        ;or
  (inst-fx! overpad fx-distortion)
  (inst-fx! overpad fx-chorus )
  (inst-fx! overpad fx-reverb)
  (clear-fx overpad)
  
  (inst-fx! dream fx-echo)
  (inst-fx! dream fx-chorus)
  (inst-fx! dream fx-reverb)
  (inst-fx! dream fx-compressor)
  ;;(dream (nth dream-samples 3))
  ;;reset, but it doesnt seem right
  (def dreamidx (agent 0))
  (dream-inc)
  (clear-fx dream)

  (choir2s)
  (inst-fx! choir2s fx-feedback-distortion)
  (inst-fx! choir2s fx-chorus )

  (clear-fx choir2s)

  ;;(choir)
  (choir4s)
  (inst-fx! choir4s fx-feedback-distortion)
  ;;you can add a bunch of chorus in serial for a really dreamy effect, but then you need to increase the sample volume
  ;;6 times chorus, 64 times vol, seems okay
  (inst-fx! choir4s fx-chorus )
  (inst-fx! choir4s fx-echo)
  (inst-fx! choir4s fx-reverb)
  (inst-fx! choir4s g-verb)
  (clear-fx choir4s)
  (stop)


  )
;; #+END_SRC

;; * some dubstep varation
;; mostly copied

;; and combining ideas from sounds.clj with the rhythm ideas here:

;; first we bring back the dubstep inst
;; #+BEGIN_SRC clojure
(definst dubstep [freq 100 wobble-freq 5]
  (let [sweep (lin-exp (lf-saw wobble-freq) -1 1 40 5000)
        son   (mix (saw (* freq [0.99 1 1.01])))]
    (lpf son sweep)))
;; #+END_SRC
;; define a vector of frequencies from a tune
;; later, we use (cycle notes) to repeat the tune indefinitely
;; #+BEGIN_SRC clojure
(def notes (vec (map (comp midi->hz note) [:g1 :g2 :d2 :f2 :c2 :c3 :bb1 :bb2
                                           :a1 :a2 :e2 :g2 :d2 :d3 :c2 :c3])))
;; #+END_SRC
;; bass is a function which will play the first note in a sequence,
;; then schedule itself to play the rest of the notes on the next beat
;; #+BEGIN_SRC clojure
(defn mybass [m num notes]
  (at (m num)
      (overpad :note (first notes)))
  (apply-at (m (inc num)) mybass m (inc num) (next notes) []))
;; #+END_SRC
;; wobble changes the wobble factor randomly every 4th beat
;; #+BEGIN_SRC clojure
(defn wobble [m num]
  (at (m num)       
      (ctl dubstep :wobble-freq
           (choose [4 6 8 16])))
  (apply-at (m (+ 4 num)) wobble m (+ 4 num) [])
  )
;; #+END_SRC
;; put it all together
;; #+BEGIN_SRC clojure
(comment
  (do
    (metro :bpm 180)
    (dubstep) ;; start the synth, so that bass and wobble can change it
    (mybass metro (metro) (cycle notes))
    (wobble metro (metro))
    )
  )
;; #+END_SRC
;; * sam aaron examples from #emacs
;; some snippets which sam aaron on #emacs shared.
;; #+BEGIN_SRC clojure
(comment
  (demo 60 (g-verb (blip (mouse-y 24
                                  48) (mouse-x 1 100)) 200 8))

  (demo 60 (g-verb (sum (map #(blip (* (midicps (duty:kr % 0 (dseq
                                                              [24 27 31 36 41] INF))) %2) (mul-add:kr (lf-noise1:kr 1/2) 3 4)) [1
                                                                                                                                1/2 1/4] [1 4 8])) 200 8))
  )
;; #+END_SRC
;; * dnb and amen beats
;; here i want some simple drum machine code to play with.

;; you can find lots of drum patterns on wikipedia, and you can convert them rather easily to lisp constructs.

;; then i combined with my other instruments to to form ... something.

;; reverse a beat: (map #(list (first %) (reverse (second %)) ) amen-beat)
;; #+BEGIN_SRC clojure

;;the dummy beat thing is a workaround, because i really wanted some kind of forward declaration
(def dummy-beat)
(def *beat (ref dummy-beat))

(def *beat-count (ref 0))

(defn drum-set-beat [beat]
  (dosync (ref-set *beat beat)))





(def amen-beat
  {
   :C '[   - - - - - - - - - - - - - - - -  - - - - - - - - - - - - - - - -  - - - - - - - - - - - - - - - -  - - - - - - - - - - X - - - - -  ]
   :R '[   x - x - x - x - x - x - x - x -  x - x - x - x - x - x - x - x -  x - x - x - x - x - X - x - x -  x - x - x - x - x - - - x - x -  ]
   :S '[   - - - - o - - o - o - - o - - o  - - - - o - - o - o - - o - - o  - - - - o - - o - o - - - - o -  - o - - o - - o - o - - - - o -  ]
   :B '[   o - o - - - - - - - o o - - - -  o - o - - - - - - - o o - - - -  o - o - - - - - - - o - - - - -  - - o o - - - - - - o - - - - -  ]
   }
  )

(def amen2-beat
  {
   :C '[   - - - - - - - - - - - - - - - -  - - - - - - - - - - - - - - - -  - - - - - - - - - - - - - - - -  - - - - - - - - - - X - - - - -  ]
   :R '[   x - x - x - x - x - x - x - x -  x - x - x - x - x - x - x - x -  x - x - x - x - x - X - x - x -  x - x - x - x - x - - - x - x -  ]
   :S '[   - - - - o - - o - o - - o - - o  - - - - o - - o - o - - o - - o  - - - - o - - o - o - - - - o -  - o - - o - - o - o - - - - o -  ]
   :B '[   o - o - - - - - - - o o - - - -  o - o - - - - - - - o o - - - -  o - o - - - - - - - o - - - - -  - - o o - - - - - - o - - - - -  ]
   }
  )

(def dnb-beat
  {

   ;;http://www.newgrounds.com/bbs/topic/662530
   :B '[ 0 - - - - - - - - - 0 - - - - - ]
   :S '[ - - - - 0 - - - - - - - 0 - - - ]
   :R '[ 0 - 0 - 0 - 0 - 0 - 0 - 0 - 0 - ]
   }
  )

(def silent-beat
  {

   ;;http://www.newgrounds.com/bbs/topic/662530
   :B '[ - - - - - - - - - - - - - - - - ]
   :S '[ - - - - - - - - - - - - - - - - ]
   :R '[ 0 - - - 0 - - - 0 - - - 0 - - - ]
   }
  )

(do
  (def psy-beat
    {
     :B2 '[ 0 - - - 0 - - - 0 - - - 0 - - - ]
     :S  '[ - - - - 0 - - - - - - - 0 - - - ]
     :H  '[ c c o - c c o - c c o - c c o - ]
                                        ;:R2 '[ - :c4  :c4 - :c4 :c4 :c4 - :c4 :c4 :c4 - :c4 :c4 :c4 ]
                                        ;   :R2 '[ - :c2  :c2 :a2 - :c2 :c2 :a2 - :c2 :c2 :a2 - :c2 :c2 :b2 ]
     :R2 '[ - :c2  :c2 :c2 - :c2 :c2 :c2 - :c2 :c2 :c2 - :c2 :c2 :c2 ]
     :R  '[ - - - - 0 - - - - - - - 0 - - - ]   
     }
    )
  (drum-set-beat psy-beat)
  )

(def my-drums
  {:C (fn [x] (hat-demo))
   :R (fn [x] (psybass2 60 :numharm (rand-int-range 10 200))
        (closed-hat) (haziti-clap)
        )

   :R2 (fn [x]
         (psybass3 (note x) )
         (psybass3 (note x) :numharm (rand-int-range 40 600))
         (psybass3 (note x) :numharm (rand-int-range 40 600))
         )
   :S (fn [x] (noise-snare) (noise-snare) (noise-snare :decay 0.4)
                                        ;(dub-kick)
        )
   :B2 (fn [x] 
         (dub-kick)
         )   
   :B (fn [x] (kick)(tom)(quick-kick))
   :H (fn [x] (if (= 'c x)(closed-hat) (open-hat)))
   }
  )

(def *drums (ref my-drums))
(defn drum-set-drums [drums]
  (dosync (ref-set *drums drums)))

(defn drum-reverse-beat [pattern]
  (map #(list (first %) (reverse (second %)) ) pattern))


(comment
  (metro :bpm 400)
  (drum-set-drums my-drums)
  (play-drums-metro metro (metro))
  (drum-set-beat amen-beat)
  (drum-set-drums my-drums)
  (drum-set-beat psy-beat)
  (drum-set-beat (drum-reverse-beat psy-beat))  
  (drum-set-beat dnb-beat)
  (drum-set-beat (drum-reverse-beat amen-beat))
  (drum-set-beat dnb-beat)
  (inst-fx! psybass2 fx-chorus)
  (inst-fx! psybass2 fx-reverb)
  (inst-fx! psybass2 fx-echo)
  (clear-fx psybass2)

  (inst-fx! psybass3 fx-chorus)
  (inst-fx! psybass3 fx-echo)  
  (inst-fx! psybass3 fx-feedback-distortion)
  (inst-fx! psybass3 fx-distortion)
  (inst-fx! psybass3 fx-compressor)
  
  (clear-fx psybass3)
  
  (inst-fx!   noise-snare fx-chorus)
  (inst-fx! noise-snare fx-echo)
  (inst-fx!   noise-snare fx-reverb)
  (clear-fx noise-snare)

  (inst-fx!   closed-hat fx-chorus)
  (inst-fx!   open-hat fx-chorus)
  (inst-fx! closed-hat fx-echo)
  (inst-fx!   closed-hat fx-reverb)
  (clear-fx closed-hat)
  (clear-fx open-hat)
  
  (inst-fx! tom fx-reverb)
  (clear-fx tom )

  
  (inst-fx! hat-demo fx-echo)

  (dubstep 25)
  (kill dubstep)
  (ctl dubstep :wobble-freqm
       (choose [4 6 8 16]))
  (stop)
  )
;; #+END_SRC
                                        ;re-def works, but not ref-set?
;;(sync (ref-set *beat amen-beat))
;;(def *beat (ref amen-beat))
;; #+BEGIN_SRC clojure

(defn drum-reverse-beat [pattern]
  "reverse PATTERN"
  (map #(list (first %) (reverse (second %)) ) pattern))

(defn drum [voice pattern]
  (dosync (alter *beat conj [voice pattern])))

(defn get-drum-fn [voice]
  "get the drum function for VOICE, from the current drum kit in *drums"

  (let* [voice1
         (if (map? voice)
           (get @*drums (:alias voice)) ;; this was dumb TODO remove
           (get @*drums voice)
           
           )]
    (if (fn? voice1) ;;TODO this isnt complete yet, 
      voice1
      (get @*drums (:alias voice1))
      )))

(defn clear-drums []
  "clear out the drum kit"
  (dosync (ref-set *beat [])))

(defn drum-fn [beat beat-count]
  "take a vertical slice out of BEAT, at BEAT-COUNT, and play this slice
allows for voice patterns of different length
undefined voices are dropped"
  (doseq [[voice pattern] beat] ;; map over each voice/pattern pair
    (let* [index (mod beat-count (count pattern) ) ;; *beat-count is global counter, make index modulo pattern length
           drum (get-drum-fn voice)] ;;figure out the drum function for the voice
      (if (and drum (not (= '- (nth pattern index)))) ;;play the drum if there a) is a drum and b) the pattern contains something that isnt "-"
        (do
          (try
            (let [nthpattern (nth pattern  index)]
              (if (sequential? nthpattern);; i want the arg to possible be like 110 or, [110 :symbol]
                (apply drum nthpattern)
                (apply drum [nthpattern]))
              )
            ;; since its not really possible atm to guarantee that the stuff in the sequence is compatible with the drum function,
            ;; errors are simply catched and ignored. otherwise the entire sequence stops which i dont want
            (catch Exception e (println "uncompatible drum fn"))
            ) 
          
          )
        )
      )))

(defn drum-fn-globalbeat []
  (drum-fn  @*beat @*beat-count)
  
  )

(defn play-drums-metro [m beat-num]
  "start playing drums, using m as metro"
  ;;play drums using a metronome strategy, which has advantages over the periodic strategy
  ;; 1st reschedule next call to the sequencer
  (apply-at (m (+ 1 beat-num))
            play-drums-metro
            m
            (+ 1 beat-num)
            [])
  ;; 2nd schedule the drum voice
  (at (m beat-num)(drum-fn-globalbeat))
  ;;3d step global counters
  (dosync (ref-set *beat-count (inc @*beat-count) ))
  )





(defn beat-max-len [beat]
  "the bars can be different lengths in a beat, so figure out the longest one"
  (reduce max (map #(count %) (map (fn [key] (get beat key)) (keys beat))))

  )


(defn play-drums-once2 [m beat-num beat count-cur count-end]
  "start playing drums, using m as metro. only play them once, unlike the sister function."
  ;;count-cur is normally 0 at the outset, and count-end the count of the longest beat bar
  ;;initially i tried using "loop/recur", and "at" instead of temporal recursion, but that didnt work for some reason
  ;;which i find odd.
  (if (> count-end count-cur) ;;limit the number of calls to the sequence length
    (do
      (println count-cur count-end)
      (apply-at (m (inc beat-num))
                play-drums-once2
                m
                (inc beat-num)
                beat
                (inc count-cur)
                count-end
                [])
      ;; 2nd schedule the drum voice
      (at (m beat-num)(drum-fn beat count-cur))

      )
    )
  

  )
(defn play-drums-once [m beat-num beat]
  (play-drums-once2 m beat-num beat 0 (beat-max-len beat))

  )


;; (beat-max-len {:A '[a b] :B '[c d e] :C '[d]})

;; a totaly different kind of song, Revenue Inspector!
(comment
  ;; do a couple of these with different pan
  (def N1 (monotron 40 0.8 1 0.0 2.5 350.0 800.0 3.0 1))
  (def N2 (monotron 40 0.8 1 0.0 2.5 350.0 800.0 3.0 0))
  (ctl N1 :cutoff 600.0)
  ;; then occasionally these, which you then kill
  (def st1 (space-theremin :out-bus 10 :amp 0.8 :cutoff 1000))
  (space-reverb [:after st1] :in-bus 10)
  (kill space-reverb)

  ;;then stop
  (stop)
  )
;; #+END_SRC

;; and now for "escape from synthmen"



(definst grainy2 [b 0]
  (let [
        trate (mouse-y:kr 1 30)
        dur (/ 2 trate)]
    ;;i do this to get stereo compatibility ith the fx
    [
     (t-grains 2 (impulse:ar trate) b 1 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2)
     (t-grains 2 (impulse:ar trate) b 1 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2)
     ]
    ))

(definst grainy3 [b 0 vol 1]
  (let [
        trate (mouse-y:kr 1 30)
        dur (/ 2 trate)]
    ;;i do this to get stereo compatibility ith the fx
    [
     (* vol (t-grains 2 (impulse:ar trate) b 0.5 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2))
     (* vol (t-grains 2 (impulse:ar trate) b 0.5 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2))
     ]
    ))


(definst grainy4 [b 0]
  (let [
        trate (mouse-y:kr 1 30)
        dur (/ 2 trate)]
    ;;i do this to get stereo compatibility ith the fx
    [
     (t-grains 2 (impulse:ar trate) b 1 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2)
     (t-grains 2 (impulse:ar trate) b 1 (mouse-x:kr 0 (buf-dur:kr b)) dur 0 0.8 2)
     ]
    ))


;;(def organ-sample (load-sample "~/samples/organ.wav"))
;; (def organ-sample (load-sample "/home/joakim/smurf.wav"))
;; (def orgloop (sample "/home/joakim/smurf.wav"))
;;                                         ;(orgloop)
;;                                         ;(stop)
;; (def dr1 (load-sample   "/home/joakim/am_i_alive_all/am_i_alive/01.wav"))
                                        ;(def glasscrash (sample (frp 221528)))
                                        ;(play-buf 1 organ-sample)
(def glasscrashsample (load-sample (frp 221528)))
(definst glasscrash [vol 1]
  (* vol (play-buf :num-channels 2 :bufnum glasscrashsample :rate 2)))


                                        ;(glasscrashinst)
(comment
  ;;reset
  (do
    (stop)
    (kill grainy2)  (clear-fx grainy2)
    (kill grainy3) (clear-fx grainy3)
    (kill grainy4) (clear-fx grainy4)
    (clear-fx glasscrash)
    )
  ;;setup
  (do
    (glasscrash)
    (drum-set-beat silent-beat)
    ;;(play-drums 100 16)
    (metro :bpm 500)
    (drum-set-drums my-drums)
    (play-drums-metro metro (metro))

    )

  ;;start/reset with mattias grain opnly
  (do
    (inst-fx! glasscrash fx-echo)
    (glasscrash)
    (kill grainy4) (clear-fx grainy4)
    (grainy4 dr1)  
    (inst-fx! grainy4 fx-echo)
    (kill grainy2)  (clear-fx grainy2)
    (kill grainy3) (clear-fx grainy3)

    )

  ;; now for some beat
  (do
    (inst-fx! glasscrash fx-echo)
    (glasscrash)
    (drum-set-beat dnb-beat)
    )

  ;; organy grains
  (do
    (inst-fx! glasscrash fx-echo)
                                        ;           (inst-fx!   glasscrash fx-reverb)
    (glasscrash)
                                        ;    (grainy2 organ-sample)
                                        ;(inst-fx!   grainy2 fx-chorus)
                                        ;(inst-fx!   grainy2 fx-reverb)
    (kill grainy4) (clear-fx grainy4)
    (grainy3 organ-sample)
    (ctl grainy3  :vol 1)
    (inst-fx!   grainy3 fx-chorus)
    (inst-fx!   grainy3 fx-reverb)
    )

  (do
    (ctl grainy3  :vol 0.25)
    (grainy4 dr1)  
    (drum-set-beat amen-beat)
    )

  
  (drum-set-beat silent-beat)

  (do
    (kill grainy4) 
    (drum-set-beat silent-beat)
    (inst-fx! glasscrash fx-echo)
    (inst-fx!   glasscrash fx-reverb)
    (glasscrash)
    )
  
  (stop) 


  
  (inst-fx! grainy3 fx-feedback-distortion)
  (clear-fx grainy3)
  (kill grainy2)
  (inst-fx! grainy2 fx-echo)  
  (inst-fx! grainy2 fx-feedback-distortion)
  (inst-fx! grainy2 fx-distortion)

  (glasscrash)
  
  (clear-fx grainy2)
  (stop)
  )

;;; now "industrial wastelands"

(def wasteland-beat
  {

   ;;http://www.newgrounds.com/bbs/topic/662530
   :B  '[ 0 - - - 0 - - - 0 - - - 0 - - -  0 - - - 0 - - - 0 - - - 0 - - - ]
   :Q  '[ 0 - - - - - - - - - - - 0 - - -  - - - - - - - - - - - - - - - - ]
   :S  '[ - - - - 0 - - - - - - - 0 - - -  - - - - 0 - - - - - - - 0 - - - ]
   :R  '[ 0 - 0 - 0 - 0 - 0 - 0 - 0 - 0 -  0 - 0 - 0 - 0 - 0 - 0 - 0 0 0 - ]
   :H  '[ 0 c c - 0 c c - 0 - c c 0 - c -  0 - c c 0 - c - 0 - c - c 0 0 - ]
   :R2 '[ :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2  ]
   :R3 '[ :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :c2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2 :e2  ]
   
   }
  )

(def hiss2 (sample (frp   66248  )))

(def hiss3 (sample (frp 119741)))
(def door (sample (frp 66817)))
(def steel (sample (frp 172421)))
(def chain (sample (frp 167915)))

(def wasteland-drums
  {:C (fn [x] (hat-demo))
   :R (fn [x]
                                        ;(psybass2 60 :numharm (rand-int-range 10 200))
        (closed-hat)
                                        ;(haziti-clap)
        )

   :R2 (fn [x]
                                        ;          (jvmonotron (+ 12(note x)) 4 0.001 1 1 0.2 2.5 350.0  (midi->hz (+ 0(note x))) 3.0 0)
         (psybass3 :note (note x) :amp 4 :numharm 10)
                                        ;       (psybass3 :note (+ 12(note x)) :amp 8 :numharm 10)
                                        ;   (psybass3 :note (+ 24(note x)) :amp 8 :numharm 10)
         (psybass3 :note (+ 19(note x)) :amp 2 :numharm 10)
         (psybass3 :note (+ 36(note x)) :amp 2 :numharm 10)
                                        ;   (psybass3 :note (+ 48(note x)) :amp 8 :numharm 10)         
         ;; (psybass3 (note x) )         (psybass3 (note x) )
         ;; (psybass3 (note x) )
                                        ;(psybass3 (note x) )         (psybass3 (note x) )         (psybass3 (note x) )
                                        ;(psybass3 (note x) :numharm (rand-int-range 40 600))
                                        ;(psybass3 (note x) :numharm (rand-int-range 40 600))
         )
   :R3 (fn [x]

                                        ;(jvmonotron (+ 0 (note x)) 4 0.001 1 1 0.2 2.5 350.0  (midi->hz (+ 0(note x))) 3.0 0)
         (if (> 20 (rand-int-range 0 100))(do    (kill simple-flute)
                                        ;(simple-flute :freq (midi->hz(+ 48 (note x))))
                                                 )
             )
                                        ;            (simple-flute :freq (midicps (+ 0 (note x))))
         (simple-flute :freq (midi->hz(+ 36 (note x))))
         )

   :S (fn [x] (noise-snare) (noise-snare) (noise-snare :decay 0.4)
                                        ;(dub-kick)
        )
   :B2 (fn [x] 
         (dub-kick)
         )
   :Q (fn [x]   (apply (choose [ (fn [] (steel))(fn [] (chain)) (fn [] (hiss3)) ]) []))
   :B (fn [x] (kick)(tom)(quick-kick))
   :H (fn [x] (if (= 'c x)(closed-hat) (open-hat)))
   }
  )

;;my monotron is the same as the example monotron except you can apply effects to it
(definst jvmonotron
  "Korg Monotron from website diagram:
   http://korg.com/services/products/monotron/monotron_Block_diagram.jpg."
  [note     60            ; midi note value
   volume   0.7           ; gain of the output
   attack 0.001 release 0.2 
   mod_pitch_not_cutoff 1 ; use 0 or 1 only to select LFO pitch or cutoff modification
   pitch    0.0           ; frequency of the VCO
   rate     4.0           ; frequency of the LFO
   int      1.0           ; intensity of the LFO
   cutoff   1000.0        ; cutoff frequency of the VCF
   peak     0.5           ; VCF peak control (resonance)
   pan      0             ; stereo panning
   ]
  (let [note_freq       (midicps note)
        env   (env-gen (perc attack release) :action FREE)
        pitch_mod_coef  mod_pitch_not_cutoff
        cutoff_mod_coef (- 1 mod_pitch_not_cutoff)
        LFO             (* int (saw rate))
        VCO             (saw (+ note_freq pitch (* pitch_mod_coef LFO)))
        vcf_freq        (+ cutoff (* cutoff_mod_coef LFO) note_freq)
        VCF             (moog-ff VCO vcf_freq peak)
        ]
                                        ;(out 0 (pan2 (* volume env VCF) pan))
    [(* volume env VCF)  (* volume env VCF) ]
    ))




(comment
  (drum-set-beat wasteland-beat)
  (drum-set-drums wasteland-drums)
  ;;  (play-drums 200 32)
  (metro :bpm 300)
  (play-drums-metro metro (metro))
                                        ;(play-drums 200 33)
  ;;120 is nice, and 200 is also nice
  (stop)
  (def syntheticmachine(sample (frp 249879))) ;synthetic machine
  
                                        ;  (def tst (sample (frp 257021))) ;mp3 cant be handled
  (def hiss (sample (frp 130291  )))
  (hiss)
  (hiss2)
  (steel)
  
  (door)
  (hiss3)
  (chain)
  (steel)


  (stop)
  (inst-fx! psybass3 fx-reverb)
  (inst-fx! psybass3 fx-echo)
  (inst-fx! psybass3 fx-chorus)
  (clear-fx psybass3 )
  
  (inst-fx! closed-hat fx-chorus)
  (clear-fx closed-hat )

  (tb303  :note 36 :wave 0 :decay 2 :amp 2 :cutoff 100 :r 0.01)
  (overpad :note 72)
  (ks1 :note 12)
  (whoahaha)
  (bubbles)
  (kill bubbles)

  (simple-flute )
  (note :c2)
  (kill simple-flute)
  (inst-fx! simple-flute fx-echo)
  (inst-fx! simple-flute fx-chorus)

  
  (jvmonotron (note :e4) 2 0.001 1 1 0.0 2.5 350.0 400.0 3.0 0)
  (inst-fx! jvmonotron fx-reverb)
  (inst-fx! jvmonotron fx-echo)
  (inst-fx! jvmonotron fx-chorus)
  (clear-fx jvmonotron )

  
  (kill jvmonotron)
  (clear-fx psybass3)
  (stop)
  )

;;pasuspender -- xeyes
;; jackd -d alsa -d hw:1,0

;; lockstep
;; a dnb song with a simple melody.



(def lockstep-drums ;lockstep-drums
  {:C (fn [x] (hat-demo))
   :R (fn [x]
                                        ;(psybass2 60 :numharm (rand-int-range 10 200))
        (closed-hat)
                                        ;(haziti-clap)
        )

   :R2 (fn [x]
         (psybass3 :note (note x) :amp 20 :numharm 10)
         (tb303  :note (note x) :r 0.5 :wave 1 :release 0.1)
         (tb303  :note (+ 12 (note x)) :r 0.9 :wave 0 :release 0.1)
                                        ;        (overpad :note (+ 12 (note x)))
                                        ;        (overpad :note (+ 12 (note :c2)))
                                        ;         (psybass3)
         )
   :R4 (fn [x]
         (tb303  :amp 2 :note (note x) :r 0.5 :wave 1 :release 0.1)
                                        ;         (tb303  :note (+ 12 (note x)) :r 0.9 :wave 0 :release 0.1)
                                        ;(overpad :note (+ 12 (note x)))
                                        ;        (overpad :note (+ 12 (note :c2)))
                                        ;         (psybass3)
         )
   
   :R3 (fn [x]

                                        ;(jvmonotron (+ 0 (note x)) 4 0.001 1 1 0.2 2.5 350.0  (midi->hz (+ 0(note x))) 3.0 0)
         (if (> 20 (rand-int-range 0 100))(do    (kill simple-flute)
                                        ;(simple-flute :freq (midi->hz(+ 48 (note x))))
                                                 )
             )
                                        ;            (simple-flute :freq (midicps (+ 0 (note x))))
         (simple-flute :freq (midi->hz(+ 36 (note x))))
         )

   :S (fn [x] (noise-snare) (noise-snare) (noise-snare :freq 1600 :decay 0.8)
                                        ;(dub-kick)
        )
   :B2 (fn [x] 
         (dub-kick)
         )
   :Q (fn [x]   (apply (choose [ (fn [] (steel))(fn [] (chain)) (fn [] (hiss3)) ]) []))
   :B (fn [x] (kick)(tom)(quick-kick))
   :H (fn [x] (if (= 'c x)(closed-hat) (open-hat)))
   }
  )

(def lockstep-beat
  {
   :B  '[ 0 - - - - - - - - - 0 - - - - - ]
   :S  '[ - - - - 0 - - - - - - - 0 - - - ]
   :H  '[ c c c 0 c c c c c c c 0 c 0 c c ]
   {:voice :R22 :seq 2}  '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
   :R2 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
                                        ;      :R3 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
   }
  )


;; c d# c d# c c c c f d# 




(comment
  (drum-set-beat dnb-beat)
  (drum-set-beat wasteland-beat)
  ;;  (play-drums 200 16)
  (metro :bpm 400)
  (play-drums-metro metro (metro))

  (drum-set-beat lockstep-beat)
  (drum-set-drums lockstep-drums)

  (clear-fx noise-snare)
  (stop)
  (kill simple-flute)


  ;;another qy to play with the beat
  (drum-set-beat
   {
    :B  '[ 0 - - - - - - - - - 0 - - - - - ]
    :S  '[ - - - - 0 - - - - - - - 0 - - - ]
    :H  '[ c c c 0 c c c c c c c 0 c 0 c c ]
    ;; {:voice :R2 :seq 2 }  '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
    :R2 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
    ;;      :R3 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
    }
   )

  (drum-set-drums my-drums)
  (drum-set-drums lockstep-drums)
  (drum-set-drums wasteland-drums)
  (stop)

  )



(def test-beat
  {
   :B  '[ 0 - - - - - - - - - 0 - - - - - ]
   :S  '[ - - - - 0 - - - - - - - 0 - - - ]
   :H  '[ c 0 c c]
   {:H :2}  '[ c 0 c c]
   :R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
   {:voice :R2} '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
   }
  )


(definst grainy5 [b 0 trate 20 dur3 1 attack 0.1 release 0.1]
  (let [
        ;;        trate (mouse-y:kr 1 30)
        dur (/ 2 trate)
        dur2  (* dur3 (buf-dur:kr b))
        env   (env-gen (perc attack release) :action FREE)
        ]
    ;;i do this to get stereo compatibility ith the fx
    [
     (* env (t-grains 2 (impulse:ar trate) b 1 dur2 dur 0 0.8 2))
     (* env (t-grains 2 (impulse:ar trate) b 1 dur2 dur 0 0.8 2))
     ]
    ))

(defn il [num seq]
    "interleave sequences with nops. for tempo experiments."
  (mapcat (fn [x] (concat (list x) (repeat num '-) )) seq))

;;drum system tests
(comment
  ;;all beats here
  (drum-set-beat silent-beat)
  (drum-set-beat psy-beat)
  (drum-set-beat dnb-beat)
  (drum-set-beat amen-beat)
  (drum-set-beat wasteland-beat)
  (drum-set-beat lockstep-beat)
  (drum-set-beat test-beat)

  ;; you dont need a symbol of course
  (drum-set-beat  {
                   :B  '[ 0 - - - - - - - - - 0 - - - - - ]
                   {:voice :H :seq 2}  '[ 0 0 0 - - - - - - - 0 0 0 - - - ]
                   :S  '[ - - - 0 - - - - - - - 0 - - - ]
                   :H  '[ c 0 c ]
                   :Hsilent  '[ c 0 c  ]
                   :R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
                   {:voice :R2 :seq 2}  '[ :c3 :d#3 :c3 :d#3 :c3   :c3 :c3 :c3 :f3 :d#3 :c3 :c3 :c3   ]
                   }
                  )


  (drum-set-beat  [
                   [:B  '[ 0 - - - - - - - - - 0 - - - - - ]]
                   [:B  '[ - 0 - -  - - - - - - - 0 - - - -  ]]
                   [:S  '[ - - - 0 - - - - - - - 0 - - - ]]
                   [:R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]]
                   [{:voice :R2 }  '[ :c3 :d#3 :c3 :d#3 :c3   :c3 :c3 :c3 :f3 :d#3 :c3 :c3 :c3   ]]
                   ]
                  )


  (drum-set-beat  [
                   [:B  '[ 0 - - - - - - - - - 0 - - - - - ]]
                   [:B  '[ - 0 - - - - - - - - - 0 - - - -  ]]
                   [:S  '[ - - - 0 - - - - - - - 0 - - - ]]
                   [:R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]]
                   [{:voice :R2 :seq 3}  '[ :c3 :d#3 :c3 :d#3 :c3   :c3 :c3 :c3 :f3 :d#3 :c3 :c3 :c3   ]]
                   ]
                  )


  ;; the drums
  (drum-set-drums my-drums)
  (drum-set-drums lockstep-drums)
  (drum-set-drums wasteland-drums)



  (drum-set-beat  {
                   :C  '[ 0 - - - - - - - - - 0 - - - - - ]
                                        ;                {:voice :H :seq 2}  '[ 0 0 0 - - - - - - - 0 0 0 - - - ]
                                        ;               :S  '[ - - - 0 - - - - - - - 0 - - - ]
                                        ;              :H  '[ c 0 c ]
                   :R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
                   :R3 '[ :c3 :d#3 :c3 :d#3 :c3   :c3 :c3 :c3 :f3 :d#3 :c3 :c3 :c3   ]
                   }
                  )


  (drum-set-drums
   {:C (fn [x] (hat-demo))
    :R2 (fn [x]
          (tb303  :note (+ 12 (note x)) :r 0.9 :wave 0 :release 0.1)
          )
    :R3 {:alias :R2}
    })
  (inst-fx!  tb303 fx-echo)

  ;;flawed?
  (drum-set-beat  {
                   :H_ '[ c  c c O ]
                   :B  '[ 0 - - - ]
                   :R2 '[ :e4 - - - :c4  - - - :d4 :e4 - - :d4 :c4 - -  ]
                   :R4 '[ :e6  :c6 :d6 :e6  :d6 :c6    ]
                   }
                  )

  (drum-set-drums lockstep-drums)
  (inst-fx!  psybass3 fx-chorus)
  (inst-fx!  psybass3 fx-freeverb)
  (inst-fx!  psybass3 fx-echo)

  (clear-fx psybass3 )
  
  ;; use metronome strategy
  (metro :bpm 250)
  (metro :bpm 300)
  (metro :bpm 400)
  (metro :bpm 4000)
  ;;what does even the bpm mean?
  (play-drums-metro metro (metro))

  (cs80lead :freq 110 :fatt 0.9)
  (ctl cs80lead :freq 220 )
  (ctl cs80lead :freq 110)
  (ctl cs80lead :gate 0)

  (ctl cs80lead :fatt 0.1)
  (kill cs80lead)

  (drum-set-beat  {
                   :C  '[ 0 - - - - - - - - - 0 - - - - - ]
                                        ;                {:voice :H :seq 2}  '[ 0 0 0 - - - - - - - 0 0 0 - - - ]
                                        ;               :S  '[ - - - 0 - - - - - - - 0 - - - ]
                   :H  '[ c 0 c ]
                   :R2 '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 x :c2 x :c2 x ]
                   :B  '[ 0 - - - ]
                                        ;                :Q '[ 0 - - - - - - - -]
                   
                   }
                  )
  (drum-set-drums
   {:R2 (fn [x]
          (if (= 'x x)
            (ctl cs80lead :gate 0)
            (do
              (ctl cs80lead :gate 1)
              (ctl cs80lead :freq         (midi->hz (+ -12 (note  x)))))))
    :B (fn [x](dub-kick) (kick    :amp-decay 2))
    :B2 (fn [x]  (tb303 :note (note x)) )
    :B3 (fn [x]  (grunge-bass :note (note x)) )
    :Q (fn [x]   (apply (choose [ (fn [] (steel))(fn [] (chain)) (fn [] (hiss3)) ]) []))
    :H (fn [x] (cond
                (= 'c x)(closed-hat)
                (= 'r ) (apply (choose [ (fn [] (closed-hat))(fn [] (open-hat)) (fn [] (hat-demo)) ]) [])
                :else (open-hat)))
    :H2 {:alias :H}
    :V (fn [x] (grainy5 dr1 (rand-int-range 1 10)  (rand)))
    :S (fn [x] (snare :freq 440 :decay 20 ))
    :KS (fn [x] (ks1   :note (note x) :decay 0.1 :coef 0.1))
    })
  (inst-fx!  cs80lead fx-chorus)
  (inst-fx!  cs80lead fx-freeverb)
  (inst-fx!  cs80lead fx-echo)
  (inst-fx!  tb303 fx-echo)
  (inst-fx!  grainy5 fx-echo)
  (inst-fx!  tb303 fx-freeverb)
  (clear-fx  tb303 )
  (clear-fx cs80lead )

  (inst-fx! grainy5 fx-echo)
  (inst-fx! grainy5 fx-echo)
  (clear-fx grainy5 )

  (grainy5 dr1 4 0.6)
  (kill grainy5)

  (drum-set-beat  {
                   :H  '[ c r - -   c - c c   - c - -  c - - -]
                   ;;              :H2  '[ r r r - - - - - - - r r r - -  ]
                   ;;            :H2  '[ r  ]
                   ;;               :S  '[ - - - 0 - - - - - - - 0 - - - ]
                   ;;              :H  '[ c 0 c ]
                   :R2 '[ :g#2  :g#2 :g#2 :a#2 :a#2 :g#2 x]
                   :B  '[ 0 - - - ]
                   :B2  '[ :c2 - - - :e2 - - -]
                   ;;              :Q '[ 0 - - - - - - - -]
                   ;;:V '[ 0 - - -]
                   ;;              :V '[ 0 ]
                   :S '[0 - - - - - - -]            
                   
                   }
                  )  


  (drum-set-beat  {
                                        ;                :H '[c x c c]
                                        ;               :H '[c ]
                   :B '[0 - - -]
                   :S '[0 - - - - - - -]                 
                   :B2 '[- :c2 :c2 :c2]
                   :B3 '[- :c3 :c3 :c3 ]
                                        ;                 :V '[- :c3 :c3 :c3 ]
                   
                                        ;              :V '[ - 0 0 0 ]                 
                   
                   }
                  )  


  

  ;; experiment
  (metro :bpm 800)
  (drum-set-beat  {
                                        ;:H  (il 0 '[c])
                   :KS (il 1 (shift (into [] (map #(note %) '(:c3 :e3 :g3  :c3))) '[ 0 1 2 3  ] -12 ))
                                        ;                 :KS (il 0 '[:c3 :e3 :g3  :c3])                 
                   :B  (il 1 '[O - -  -] )
                   :S  (il 1 '[0 - - - - - - -])
                   :B2 (il 1 '[- :c2 :c2 :c2])
                   :B3 (il 1 '[- :c3 :c3 :c3 ])
                   
                   }
                  )  


  (inst-fx! grunge-bass fx-chorus)
  (inst-fx! ks1 fx-echo)
  (inst-fx! closed-hat fx-chorus)
  (inst-fx! ks1 fx-freeverb)
  (inst-fx! grunge-bass fx-freeverb)
  (clear-fx grunge-bass)
  (daf-bass :freq (note 36))
  (kill daf-bass)
  (stop)
  (kill simple-flute)

  ;;some more features id like:
  ;; mute a voice
  ;; somehow allow more patterns for a voice(perhaps with syms like :B:2
  (overtone.helpers.string/split-on-char (name :b:2) ":")  
  )


;;;; new insts 20170112
(definst radio [ freq  440 amp  0.5 gate  1]
  (let 
      [oscfreq  (repeat 3 (* freq (lin-lin (lf-noise2:kr 0.5) 0.98 1.02))) ; !3, seems to mean repeat 3 times? weird
       snd      (splay (* (lf-saw oscfreq ) amp))
       env      (env-gen (adsr 0.7 4 0 0.1) :action FREE)
       output   (lpf snd (+ (* env freq) (* 2 freq)))
       ] [output output]))



;;http://sccode.org/1-523
(definst electro-kick [out  0 pan  0 amp  0.3]
  (let
      [bodyFreq (env-gen (envelope [261 120 51] [0.035 0.08] :exp))
       bodyAmp (env-gen (lin 0.005 0.1 0.3 ):action FREE)
       body (* (sin-osc bodyFreq)  bodyAmp)

       popFreq (x-line:kr 750, 261, 0.02)
       popAmp (* (env-gen (lin 0.001 0.02 0.001))  0.15 )
       pop  (* (sin-osc popFreq) popAmp)

       clickAmp (* ( env-gen (perc 0.001 0.01)) 0.15)
       click    (* (lpf (formant 910 4760 2110) 3140)  clickAmp)

       snd (tanh (+  body pop click))
       ]
    [snd snd]))


(definst electro-hat
  [out  0, pan 0, amp  0.3]

  ;;// noise -> resonance -> expodec envelope
  (let
      [noiseAmp  (env-gen (perc 0.001, 0.3, :curve -8) :action FREE)
       noise (* (mix (bpf  (clip-noise)   [4010, 4151]
                           [0.15, 0.56]
                           ;;[1.0, 0.6] ;;why doeesnt this work?
                           ))
                0.7  noiseAmp)

       snd  noise]
    [snd snd]))


(definst electro-clap [out  0 amp  0.5 pan  0 dur  1]
  (let
      [env1   (env-gen   (envelope
                          [0, 1, 0, 0.9, 0, 0.7, 0, 0.5, 0],
                          [0.001, 0.009, 0, 0.008, 0, 0.01, 0, 0.03],
                          [0, -3, 0, -3, 0, -3, 0, -4]) :action FREE)
       noise1 (bpf (lpf (hpf (*  env1 (white-noise)) 600) (x-line 7200, 4000, 0.03)) 1620 3)

       env2 ( env-gen (envelope [0, 1, 0], [0.02, 0.18], [0, -4]))
       noise2  (bpf (lpf (hpf (*  env2 (white-noise)) 1000) 7600) 1230 0.7 ;0.7
                    )
       ]
    (softclip (* 2 (+ noise1 noise2)))
    ))

(defn loop-beats [time]
  (at (+    0 time)  (electro-hat)(electro-kick) )
  (at (+  100 time) (electro-hat)  )  
  (at (+  200 time) (electro-hat)  )
  (at (+  400 time) (electro-hat)  )
  (at (+  600 time) (electro-hat)  )
  (at (+  800 time)   (electro-hat)(electro-kick) )
  (at (+  1000 time) (electro-hat)  )  
  (at (+ 1200 time)(electro-clap) (electro-hat)  )
  (at (+  1400 time) (electro-hat)  )    
  (apply-at (+ 1600 time) loop-beats (+ 1600 time) []))


(drum-set-drums {
                 :H (fn [x] (if (= 'c x)(electro-hat) (open-hat)))
                 :B (fn [x] (electro-kick))
                 :C (fn [x] (electro-clap))
                 })

(drum-set-beat
 {
  :H '[c c c -  c - c -  c - c -  c - c -]
  :B '[c - - -  - - - -  c - - -  - - - -]
  :C '[- - - -  - - - -  - - - -  c - - -]
  })

(comment
  (loop-beats (now))
  (stop)
  (metro :bpm 600)
  (play-drums-metro metro (metro))
  )

;;debug leaky inst
;; hat fail after a while, kick as well, clap as well!
;; tstbass fails, does anything not fail=
;; (defn loop-beats [time]
;;   (at (+    0 time)  (tstbass) )
;;   (apply-at (+ 50 time) loop-beats (+ 50 time) []))


;;questions:
;; - bpf last arg
;; - get after a while: FAILURE /s_new too many nodes, so im missing some reclamation

;; some other basses

(definst tstbass [atk 0.01, dur 0.15, freq 50, amp 0.8]

  (* (bpf (lf-saw freq), freq, 2)  (env-gen(perc atk, dur, amp, 6 )))
  )



(definst bazz[ dur 0.15, freq 50, amp 0.8, index 10 ]
  (* (pm-osc freq, (+ freq  5), index) (env-gen(triangle dur ))))


(definst basicfm [out  0, gate  1, amp  1, carFreq  1000, modFreq  100, modAmount  2000, clipAmount  0.1]
  (let [modEnv (env-gen (adsr 0.5 0.5 0.7 0.1 :peak-level modAmount) gate)
        mod (* (sin-osc modFreq) modEnv)
        car (+ mod (sin-osc carFreq))
        ampEnv (env-gen (adsr 0.1, 0.3, 0.7, 0.2, :peak-level amp) gate )
        clipv (* clipAmount 500)
                                        ;snd (clip (* car ampEnv clipv) -0.7 0.7)
        snd (* 0.1 (clip:ar (* car ampEnv clipv) -0.7 0.7))
        ]
    [snd snd]
    ))

;; SynthDef(\vocali, { arg f1, f2, fund = 70, amp = 0.25 ;
;; 		var source = Saw.ar(fund);
;; 		var vowel = Normalizer.ar(BPF.ar(source, f1, 0.1))
;; 		+
;; 		Normalizer.ar(BPF.ar(source, f2, 0.1))
;; 		* amp ;
;; 		Out.ar(0, vowel.dup)
;; 	}).add ;
;; \i:[2300, 300], \e: [2150, 440], \E: [1830, 580],
;; 		\a: [1620, 780], \O: [900, 580], \o: [730, 440],
;; 		\u: [780, 290],\y: [1750, 300],\oe: [1600, 440],
;; 		\OE: [1400, 580]



(def vocali-x {:i [2300, 300], :e [2150, 440], :E [1830, 580],
               :a [1620, 780], :O [900, 580], :o [730, 440],
               :u [780, 290], :y [1750, 300],:oe [1600, 440],
               :OE [1400, 580]})





(definst vocali [f1 2300 f2 300   fund 70 amp 0.25]
  (let [
        src (saw fund)
        env (env-gen (perc 0.01 0.5) :action FREE)
        vowel (* env amp (+ (normalizer (bpf src f1 0.09))
                            (normalizer (bpf src f2 0.09))))
        ]
    [vowel vowel])
  
  )

(defn vocali2 [x fund amp]
  (let [[f1 f2] (get  vocali-x x)
        ]
    (vocali f1 f2 fund amp)
    )
  )
;;(vocali2 :i 70 0.25)  
(defn loop-beats [time]
  (at (+    0 time)  (vocali2 :a 70 0.25)(electro-hat)(electro-kick) )
  (at (+  200 time) (vocali2 :e 70 0.25)(electro-hat)  )
  (at (+  100 time) (vocali2 :i 70 0.25)(electro-hat)  )  
  (at (+  400 time) (vocali2 :E 70 0.25)(electro-hat)  )
  (at (+  600 time) (vocali2 :OE 70 0.25)(electro-hat)  )
  (at (+  800 time)   (vocali2 :y 70 0.25)(electro-hat)(electro-kick) )
  (at (+  1000 time) (vocali2 :u 70 0.25)(electro-hat)  )  
  (at (+ 1200 time) (vocali2 :o 70 0.25)( electro-clap) (electro-hat)  )
  (at (+  1400 time) (vocali2 :a 70 0.25)(electro-hat)  )    
  (apply-at (+ 1600 time) loop-beats (+ 1600 time) []))

(drum-set-drums {
                 :V (fn [x] (vocali2 x 70 0.5))
                 :B (fn [x] (electro-kick))
                 :H (fn [x] (electro-hat))
                 })
(drum-set-beat  {
                 :V '[:a :i :o :O :E :e :OE :y :u :oe ]
                 :B '[x - ]
                 :H '[x]
                 })
;; (play-drums-metro metro (metro))
;;   (metro :bpm 200)
;; chorus + echo on the vocali, sounds interesting

;;sounds pleasant with echo
(definst my-formant [fund 100]
  (let
      [
       src1 (formant fund (x-line 2000 400 ) 200)
       src2 (formant fund (x-line 2000 400 ) 400)
       env (env-gen (perc 0.01 0.8) :action FREE)
       ]
    [ (* env src1) (* env src2) ]))

(comment
  (loop-beats (now))
  (demo  (formlet (impulse:ar 50, 0.0) (mouse-x:kr 300 3000) 0.01 (mouse-x:kr 0.1 1.0)))
  (stop)
  )

(definst myformlet [freq 50 phase 0.5]
  (let
      [        env (env-gen (perc 0.01 0.5) :action FREE)
       src (impulse:ar freq, phase ) 
       src (formlet  src 300 0.01 0.1)
       src (* env src)
       ]
    [src src]
    ))


;; SynthDef(\risset, {|out = 0, pan = 0, freq = 400, amp = 0.1, dur = 2, gate = 1|
;; 		var amps = #[1, 0.67, 1, 1.8, 2.67, 1.67, 1.46, 1.33, 1.33, 1, 1.33];
;; 		var durs = #[1, 0.9, 0.65, 0.55, 0.325, 0.35, 0.25, 0.2, 0.15, 0.1, 0.075];
;; 		var frqs = #[0.56, 0.56, 0.92, 0.92, 1.19, 1.7, 2, 2.74, 3, 3.76, 4.07];
;; 		var dets = #[0, 1, 0, 1.7, 0, 0, 0, 0, 0, 0, 0];
;; 		var doneActionEnv = EnvGen.ar(Env.linen(0, dur, 0), gate, doneAction: 2);
;; 		var src = Mix.fill(11, {|i|
;; 			var env = EnvGen.ar(Env.perc(0.005, dur*durs[i], amps[i], -4.5), gate);
;; 			SinOsc.ar(freq*frqs[i] + dets[i], 0, amp*env);
;; 		});
;; 		src = src * doneActionEnv * 0.5; // make sure it releases node after the end.
;; 		Out.ar(out, Pan2.ar(src, pan));
;; 	}).add;

(definst risset [out  0, pan  0, freq  400, amp  0.1, dur  2, gate  1]
  (let
      [
       amps  [1, 0.67, 1, 1.8, 2.67, 1.67, 1.46, 1.33, 1.33, 1, 1.33]
       durs  [1, 0.9, 0.65, 0.55, 0.325, 0.35, 0.25, 0.2, 0.15, 0.1, 0.075]
       frqs  [0.56, 0.56, 0.92, 0.92, 1.19, 1.7, 2, 2.74, 3, 3.76, 4.07]
       dets  [0, 1, 0, 1.7, 0, 0, 0, 0, 0, 0, 0]
       doneActionEnv  (env-gen(lin 0, dur, 0), gate, :action FREE)


       src  (mix (map (fn [ampi duri frqsi detsi]
                        (* amp (env-gen(perc 0.005, (* dur duri), ampi, -4.5), gate)
                           (sin-osc (+ (* freq frqsi)  detsi, 0, (* amp 0.1))))) ;;0.1 should be amp
                      amps durs frqs dets))
       src (* doneActionEnv src 0.5)
       ]
    [src src]
    ))
;;sounds nice with some echo and chorus
;;(risset :freq 100 :amp 0.5)(risset :freq 800 :amp 0.1)

;;;;;;;       

;; playing with sync-saw
;;(demo [(sync-saw 50 (* 50 (line 1 1.5 1)) ) (sync-saw 51 (* 51 (line 1 1.5 1)) )])

;; i want to make a string sound
;;https://www.attackmagazine.com/technique/tutorials/analogue-style-string-synthesis/2/
;; my attempt sounds nothing like theirs :( but it sounds nice anyway
(definst mystr [freq 440]
  (let
      [fenv   (env-gen (perc 0.5 5) :action FREE)
       freqlfo  (lin-lin (lf-tri:kr  3.4) -1 1 1 1.01)
       apulsel (pulse:ar (* freqlfo freq) (lin-lin (sin-osc:kr 6) -1 1 0.5 0.6  ))
       apulser (pulse:ar (* freqlfo freq) (lin-lin (sin-osc:kr 6) -1 1 0.5 0.8  ))       
       asaw1 (lf-tri:ar (* 2 (* freqlfo freq)))
       asaw2 (lf-tri:ar (/ (* freqlfo freq) 2))
       srcl (+  (* 0.1 asaw1)  (* 0.1 asaw1)
                apulsel)
       srcr (+  (* 0.1 asaw1)  (* 0.1 asaw1)
                apulser)

       srcl (rlpf srcl (* fenv 1500) 2 )
       srcr (rlpf srcr (* fenv 1500) 2 )
       ]
    [srcl srcr]))

(defn mystr2 [f] (mystr f)(mystr (* 2 f))(mystr (* 4 f) )(mystr (* 8 f)))
(comment
  (inst-fx! mystr fx-chorus)
  (inst-fx! mystr fx-echo)

  
  (mystr2 50)
  (mystr2 150)
  (mystr2 100)
  )

;;(demo (+ (* 0.1 (saw:ar 880))(rlpf (pulse:ar 440 (lin-lin (sin-osc:kr 6) -1 1 0.5 0.55  )) 1500 1 )))

;;now i want a rim shot
;; https://www.freesound.org/people/Sajmund/sounds/132418/
;; again my rimshot sounds nothing like i want it to
(definst rimshot
  [;freq   100
   amp    0.3
   decay  0.1]
  (let [env  (env-gen (adsr-ng :attack 0 :attack-level 1 :decay 0.1 :level 0.01 :sustain 0.001 :release 2 ) :action FREE)
        fenv (lin-lin env 1 0 1800 1);(* freq 1.5) (/ freq 1.5))
        snd0 (white-noise);(white-noise:ar)
        snd (lpf snd0 fenv)
        snd1 (* 0.1 (bpf snd0 300 4))
        snd2 (* 0.1 (bpf snd0 1000 4))
        snd3 (* 0.1 (bpf snd0 1800 4))

        ]
    (* (+ snd
          snd1
          snd2
          snd3
          ) env amp)))
;;(rimshot)
;;(snare)

;; now i want to make a sound like a percussive bass, with a certain freq env
(definst tsts [freq 110]
  (let [
        env  (env-gen (perc 0.01 0.5)  :action FREE)
        fenv (env-gen (lin 0.5 0.5 0.5 0.5))
        src  (pulse:ar freq fenv)
        src2  (formlet src (/ freq 2) 0.01 0.05)
        src (+ (* 0.5 src2) src)
        src  (* env src)

        ]
    [src src]
    ))
(drum-set-drums {
                 :V (fn [x] (tsts (/  x 1)))
                 :B (fn [x] (electro-kick) (dance-kick))
                                        ;:H (fn [x] (electro-hat))

                 :H (fn [x] (cond
                             (= 'c x)(electro-hat)
                             (= 'r x) (apply (choose [ (fn [] (closed-hat))(fn [] (open-hat)) (fn [] (hat-demo)) ]) [])
                             :else (open-hat)))
                 :V2 (fn [x] (bass x))
                 :S (fn [x] (electro-clap))
                 :m (fn [x y] (println x y) )
                 })
(drum-set-beat  {
                 :V '[110 220 440 ]
                 :B '[x - ]
                 :H '[c c r]
                 :V2 '[110 110 110 55 -]
                 :S '[- - - x]
                 :m '[[a 1] [b 2] [c 3]]
                 })
;;some echo and chorus on tsts and your good!
(comment
  (play-drums-metro metro (metro))
  (metro :bpm 250)
  (inst-fx! tsts fx-echo)
  (inst-fx! tsts fx-chorus)

  (cs80lead :freq 220 :fatt 0.9)
  (ctl cs80lead :freq 220 )
  (ctl cs80lead :freq 110)
  (inst-fx! cs80lead fx-chorus)
  (kill cs80lead)
  (play-drums-once metro (metro) {
                                  :V2 '[ 220 440 880]
                                  :H '[c c r]
                                  } )
  (stop)
  )

;; some process stuff for use in the networked case
(sh/programs cvlc)
(def *cvlc (cvlc  "jack://channels=2:ports=.*" "--sout" "#transcode{vcodec=none,acodec=opus,ab=128,channels=2,samplerate=44100}:rtp{port=1234,sdp=rtsp://0.0.0.0:9999/test.sdp" {:background true}))

;; Local Variables:
;; lentic-init: lentic-clojure-org-init
;; End:
