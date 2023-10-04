OVERVIEW

This is a log aggregation system using syslog-ng, Kafka, and ElasticSearch.
It aggregates logs from containers, which can be viewed in real-time or after the fact, using a command line tool called logvis-tail.

STARTING UP

$chmod +x ./deps/init.sh
$./deps/init.sh

This will run a syslog server that will listen for any logs on UDP 514 port,
a Kafka broker, a single node ElasticSearch cluster, and a SpringBoot app called LOLA.
Any logs read will be forwarded to a Kafka topic called syslog with a key that is the tag
mentioned in the next section.
The Kafka topic will be consumed by a listener configured in LOLA, which will parse the syslog message and redirect it to an appropriate index in ElasticSearch.

The other function that LOLA serves is to act a server for tailing logs and viewing historical log data.
The client for it called logvis-tail.
It supports these commands:
 $cd logvis-tail/client
 * list logs: 
    $python3 logvis-tail.py --app list
 * tail logs:
    $python3 logvis-tail.py --app hellosb -f
 * see historic log data (between start end end datetimes):
    $python3 logvis-tail.py --app hellosb -s 2023-10-03T15:54:55.000+0000 \
     -e 2023-10-03T15:59:55.000+0000   

RUNNING A CONTAINER THAT SENDS LOGS

To build the sample app hellosb:

$sudo docker build ./hello -t devj2019/hellosb

Running hellosb with logs forwarded to syslog:

$docker run --name=hellosb1 --rm --log-driver syslog \
    --log-opt syslog-address=udp://172.30.0.10:514 \
    --log-opt tag="hellosb-{{.ID}}" devj2019/hellosb

The tag uniquely identifies an application and a container. Here it is application-<container-id>. This tag is shared by all containers of this image.
Thus, all logs coming from containers running the same application will end up in the same ES index.

TEAR DOWN

$chmod +x ./deps/teardown.sh
$./deps/teardown.sh

This will remove the containers setup in STARTING UP phase.
Thats kafka, syslog, elasticsearch and lola.

You will need to manually stop any devj2019/hellosb containers you spin up.