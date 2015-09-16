FROM fedora

RUN yum  -y install git gcc

RUN yum -y install libmicrohttpd-devel jansson-devel libnice-devel \
   openssl-devel libsrtp-devel sofia-sip-devel glib-devel \
   opus-devel libogg-devel libini_config-devel pkgconfig gengetopt \
   libtool autoconf automake
RUN yum -y install make libcurl-devel

RUN git clone https://github.com/meetecho/janus-gateway.git

RUN cd janus-gateway && sh autogen.sh
RUN  cd janus-gateway && ./configure --disable-websockets --disable-data-channels --disable-rabbitmq --disable-docs --prefix=/opt/janus && make
RUN  cd janus-gateway && make configs install 


#from an attempt to reuse a public janus docker
#FROM benmarch/janus:v0.1.1

# add janus configuration
#ADD janus.cfg /etc/janus/janus.cfg
