import json
import requests

FCM_URL = "https://fcm.googleapis.com/fcm/send"


def send_data_message(key, device_token, payload: dict):
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'key={key}'
    }
    body = {
        'data': payload,
        'to': device_token,
        'priority': 'high'
    }
    response = requests.post("https://fcm.googleapis.com/fcm/send", headers=headers, data=json.dumps(body))
    print(f'Firebase message status code: {response.status_code}')
