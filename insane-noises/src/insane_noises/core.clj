;; * namespace declaration
;; the overtone live and core seems to clash, so you need to select one of them
;;(load-file "/home/joakim/insane-noises/src/insane_noises/core.clj")

;; and a little sine wave
;; #+BEGIN_SRC clojure

(ns insane-noises.core
  (:use
   [overtone.live]
;;  [overtone.core]
   [overtone.inst synth piano drum]
))


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

  ;;"/home/joakim/jave/fromchopper20130819/music/drums/test.wav"
  (freesound-path [x])
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
                      
                      (bpf (saw [freq note ]) (+ 30 numharm) )))

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
  (def s1 (load-sample   "/home/joakim/roles/jave/music/am_i_alive_all/am_i_alive/01.wav"))
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

;; you can find lots of drum patterns on wikipedia, and you can convert them rather easily to liasp constructs.

;; then i combined with my other instruments to to form ... something.

;; reverse a beat: (map #(list (first %) (reverse (second %)) ) amen-beat)
;; #+BEGIN_SRC clojure

(def *drums (ref dnb-beat))
(def *drum-count (ref 0))

  (defn drum-set-beat [beat]
    (dosync (ref-set *drums beat)))

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


  (defn drum-reverse-beat [pattern]
    (map #(list (first %) (reverse (second %)) ) pattern))


(comment
  (play-drums 100 16)
  (drum-set-beat amen-beat)
  (drum-set-beat psy-beat)
  (drum-set-beat (drum-reverse-beat psy-beat))  
    (drum-set-beat dnb-beat)
    (drum-set-beat (drum-reverse-beat amen-beat))
  (drum-set-beat dnb-beat)
  (inst-fx! psybass2 fx-chorus)
  (inst-fx! psybass2 fx-reverb)
  (inst-fx! psybass2 fx-echo)
  (clear-fx psybass2)
  
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
  (ctl dubstep :wobble-freq
       (choose [4 6 8 16]))
  (stop)
  )
;; #+END_SRC
;re-def works, but not ref-set?
;;(sync (ref-set *drums amen-beat))
;;(def *drums (ref amen-beat))
;; #+BEGIN_SRC clojure

(defn drum-reverse-beat [pattern]
  (map #(list (first %) (reverse (second %)) ) pattern))

(defn drum [voice pattern]
  (dosync (alter *drums conj [voice pattern])))

(defn drum-set-beat [beat]
  (dosync (ref-set *drums beat)))


(defn clear-drums []
  (dosync (ref-set *drums [])))


(defn drum-fn [beat-count]
              (let [num (rand)
                    i   @*drum-count]
                (doseq [[voice pattern]
                        @*drums
                        ]
;;                  (println voice (nth pattern i)) ;; for debug, annoying in the repl
                  ;;(kick)
                  (if (not (= '- (nth pattern i)))
                    (do
                      
                      (apply (get my-drums voice) [(nth pattern i)])
                      )
                     )

                  )

                (dosync (ref-set *drum-count (mod (inc i) beat-count)))))


(defn play-drums [tempo beat-count]
  (periodic tempo
            (fn []
              (drum-fn beat-count)
              )))
;; #+END_SRC
;; Local Variables:
;; lentic-init: lentic-clojure-org-init
;; End:
