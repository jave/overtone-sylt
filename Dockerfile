FROM fedora

RUN yum -y install java-1.8.0-openjdk-devel
RUN yum -y install wget
RUN wget -q -O /usr/bin/lein \
    https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein \
    && chmod +x /usr/bin/lein
ENV LEIN_ROOT true
RUN lein
RUN yum -y localinstall --nogpgcheck http://download1.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm http://download1.rpmfusion.org/nonfree/fedora/rpmfusion-nonfree-release-$(rpm -E %fedora).noarch.rpm
RUN yum -y install vlc
# WARNING, horrible hack to enable vlc to run as root!
RUN sed -i 's/geteuid/getppid/' /usr/bin/vlc
RUN yum -y install jack-audio-connection-kit vlc-plugin-jack
RUN yum -y install jack-audio-connection-kit-example-clients fftw


ADD sylt sylt
#RUN cd insane-noises && lein deps


ADD startup.sh startup.sh
#nrepl port
EXPOSE 32768
#sylt web port including web repl
EXPOSE 3000
#rtp port
EXPOSE 1234
#rtps port 9999
EXPOSE 9999
CMD ./startup.sh
