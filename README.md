# Test

$ curl http://localhost:8080/get
$ curl --dump-header - --header 'Host: www.circuitbreaker.com' http://localhost:8080/delay/3

# Troubleshooting

## ERROR 2951 --- [ctor-http-nio-3] i.n.r.d.DnsServerAddressStreamProviders  : Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS. Check whether you have a dependency on 'io.netty:netty-resolver-dns-native-macos'. Use DEBUG level to see the full stack: java.lang.UnsatisfiedLinkError: failed to load the required native library

https://junho85.pe.kr/2054
