import requests, argparse, sys
import sseclient

LOLA_LOGS_HOST = "http://localhost:8081"
LOLA_LOGS_ENDPOINT = "logs"
LOLA_TAIL_ENDPOINT = "tail-logs"
LOLA_LIST_ENDPOINT = "list-apps"

def list_apps():
    url = f"{LOLA_LOGS_HOST}/{LOLA_LIST_ENDPOINT}"
    response = requests.get(url)
    r_json = response.json()
    for el in r_json:
        print(el)

def tail_logs(app):
    headers = {'Accept': 'text/event-stream'}
    url = f'{LOLA_LOGS_HOST}/{LOLA_TAIL_ENDPOINT}/{app}'
    response = requests.get(url, stream=True, headers=headers)
    client = sseclient.SSEClient(response)
    for event in client.events():
        print(event.data)

def fetch_and_print_logs(app, start, end):
    start = requests.utils.quote(start)
    end = requests.utils.quote(end)
    url = f"{LOLA_LOGS_HOST}/{LOLA_LOGS_ENDPOINT}/{app}?start={start}&end={end}"
    response = requests.get(url)
    r_json = response.json()
    for el in r_json:
        print(el[0] + " : " + el[1])

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--app', help='application whose logs you want, enter list to list all available', required=True)
    parser.add_argument('-f', help='tail logs', action='store_true')
    parser.add_argument('-s', help='start time e.g 2023-10-03T15:59:55.000+0000')
    parser.add_argument('-e', help='end time e.g 2023-10-03T15:59:55.000+0000')
    args = parser.parse_args()

    if args.app == "list":
        list_apps()
    elif args.f and (args.s or args.e):
        print("Can't have both f and either/both of s and e as params")
        sys.exit(1)
    elif args.f:
        tail_logs(args.app)
    if args.s and args.e:
        fetch_and_print_logs(args.app, args.s, args.e)
