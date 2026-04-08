import requests
import sys
import logging
from uuid import uuid4

logging.basicConfig(level=logging.INFO, format = '%(levelname)s: %(message)s')

ACCOUNT_API_URL = "http://localhost:8081/api/accounts"
TRANSFER_API_URL = "http://localhost:8082/api/transfers"
TIMEOUT = 5

test_id = uuid4().hex[:8].upper()

def test_critical_path():
    logging.info("Starting smoke tests: testing critical path")
    sender_account = {"accountNumber": f"TEST-{test_id}-001", "ownerName": "sender", "balance": 500.0, "currency": "PLN"}
    receiver_account =  {"accountNumber": f"TEST-{test_id}-002", "ownerName" : "receiver", "balance": 300.0, "currency": "PLN"}

    try:
        sender_resp = requests.post(ACCOUNT_API_URL, json=sender_account, timeout=TIMEOUT)
        if sender_resp.status_code != 201:
            logging.error(
                f"Failed to create sender account. "
                f"Status: {sender_resp.status_code}, body: {sender_resp.text}"
            )
            sys.exit(1)

        receiver_resp = requests.post(ACCOUNT_API_URL, json=receiver_account, timeout=TIMEOUT)
        if receiver_resp.status_code != 201:
            logging.error(
                f"Failed to create receiver account. "
                f"Status: {receiver_resp.status_code}, body: {receiver_resp.text}"
            )
            sys.exit(1)

        logging.info("Test accounts created successfully")

        transfer = {
            "fromAccount": sender_account["accountNumber"],
            "toAccount": receiver_account["accountNumber"],
            "amount": 100.0,
            "title": "Smoke test transfer",
            "currency": "PLN"
        }

        transfer_resp = requests.post(TRANSFER_API_URL, json=transfer, timeout = TIMEOUT)

        if transfer_resp.status_code != 201:
            logging.error(f"Transfer rejected. HTTP Status: {transfer_resp.status_code}, body: {transfer_resp.text}")
            sys.exit(1)


        transfer_data = transfer_resp.json()
        transfer_id = transfer_data.get("id")
        transfer_status = transfer_data.get("status")

        if not transfer_id:
            logging.error("Transfer response did not contain an ID")
            sys.exit(1)

        if transfer_status != "COMPLETED":
            logging.error(
                f"Transfer was not completed. "
                f"Status: {transfer_status}, failureReason: {transfer_data.get('failureReason')}"
            )
            sys.exit(1)

        verify_transfer_resp = requests.get(f"{TRANSFER_API_URL}/{transfer_id}", timeout=TIMEOUT)

        if verify_transfer_resp.status_code != 200:
            logging.error(
                f"Failed to fetch transfer {transfer_id}. "
                f"Status: {verify_transfer_resp.status_code}"
            )
            sys.exit(1)

        verify_transfer_data = verify_transfer_resp.json()

        if verify_transfer_data.get("status") != "COMPLETED":
            logging.error(
                f"Stored transfer has invalid status: {verify_transfer_data.get('status')}"
            )
            sys.exit(1)

        sender_check_resp = requests.get(
            f"{ACCOUNT_API_URL}/number/{sender_account['accountNumber']}",
            timeout=TIMEOUT
        )
        if sender_check_resp.status_code != 200:
            logging.error("Failed to fetch sender account after transfer")
            sys.exit(1)

        receiver_check_resp = requests.get(
            f"{ACCOUNT_API_URL}/number/{receiver_account['accountNumber']}",
            timeout=TIMEOUT
        )
        if receiver_check_resp.status_code != 200:
            logging.error("Failed to fetch receiver account after transfer")
            sys.exit(1)

        sender_balance = float(sender_check_resp.json().get("balance"))
        receiver_balance = float(receiver_check_resp.json().get("balance"))

        if sender_balance != 400.0:
            logging.error(f"Sender balance invalid. Expected 400.0, got {sender_balance}")
            sys.exit(1)

        if receiver_balance != 400.0:
            logging.error(f"Receiver balance invalid. Expected 400.0, got {receiver_balance}")
            sys.exit(1)

        logging.info("Smoke test passed successfully")

    except requests.exceptions.RequestException as e:
        logging.error(f"Connection failed: {e}")
        sys.exit(1)



if __name__ == "__main__":
    test_critical_path()
    sys.exit(0)

