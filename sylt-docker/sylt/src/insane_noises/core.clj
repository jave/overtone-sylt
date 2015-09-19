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

;; you can find lots of drum patterns on wikipedia, and you can convert them rather easily to liasp constructs.

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
  (map #(list (first %) (reverse (second %)) ) pattern))

(defn drum [voice pattern]
  (dosync (alter *beat conj [voice pattern])))

(defn get-drum-fn [voice]
  "get the drum function for the voice"

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
  (dosync (ref-set *beat [])))



;;allows for voice patterns of different length
;;undefined voices are dropped
(defn drum-fn2 []
              (let [i   @*beat-count]
                (doseq [[voice pattern]
                        @*beat
                        ]
                  (let* [index (mod i (count pattern) )
                        drum (get-drum-fn voice)]
                  (if (and drum (not (= '- (nth pattern index))))
                    (do
                      (apply drum [(nth pattern  index)])
                      )
                     )
                  ))
                (dosync (ref-set *beat-count (inc @*beat-count) ))))




;;play drums using a metronome strategy, which has advantages over the periodic strategy
(defn play-drums-metro [m beat-num]
  ;; reschedule next call to the sequencer
  (apply-at (m (+ 1 beat-num))
            play-drums-metro
            m
            (+ 1 beat-num)
            [])
  ;; schedule the drum voice
  (at (m beat-num)(drum-fn2))
  )

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
(def organ-sample (load-sample "/home/joakim/smurf.wav"))
(def orgloop (sample "/home/joakim/smurf.wav"))
                                        ;(orgloop)
                                        ;(stop)
(def dr1 (load-sample   "/home/joakim/am_i_alive_all/am_i_alive/01.wav"))
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
 ; {:voice :R2 :seq 2 }  '[ :c2 :d#2 :c2 :d#2 :c2   :c2 :c2 :c2 :f2 :d#2 :c2 :c2 :c2 :c2 :c2 :c2 ]
   :R2 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
;      :R3 '[ :c2 - - - :d#2 - - - - - :c2 - :d#2 :c2   - -   ]
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
  ;              :H2  '[ r r r - - - - - - - r r r - -  ]
    ;            :H2  '[ r  ]
  ;               :S  '[ - - - 0 - - - - - - - 0 - - - ]
   ;              :H  '[ c 0 c ]
                 :R2 '[ :g#2  :g#2 :g#2 :a#2 :a#2 :g#2 x]
                 :B  '[ 0 - - - ]
                 :B2  '[ :c2 - - - :e2 - - -]
   ;              :Q '[ 0 - - - - - - - -]
             ;:V '[ 0 - - -]
                                        ;              :V '[ 0 ]
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


(defn il [num seq]
  "interleave sequences with nops. for tempo experiments."
  (mapcat (fn [x] (concat (list x) (repeat num '-) )) seq))

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
;; some process stuff for use in the networked case
(sh/programs cvlc)
(def *cvlc (cvlc  "jack://channels=2:ports=.*" "--sout" "#transcode{vcodec=none,acodec=opus,ab=128,channels=2,samplerate=44100}:rtp{port=1234,sdp=rtsp://0.0.0.0:9999/test.sdp" {:background true}))
  
;; Local Variables:
;; lentic-init: lentic-clojure-org-init
;; End:
