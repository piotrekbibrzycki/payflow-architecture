import requests
import sys
import logging

logging.basicConfig(level=logging.INFO, format = '%(levelname)s: %(message)s')

ACCOUNT_API_URL = "http://localhost:8081/api/accounts"
TRANSFER_API_URL = "http://localhost:8082/api/transfers"

def test_critical_path():
    logging.info("Starting smoke tests, critical path execution")
    acc1 = {"accountNumber": "TEST-001", "ownerName": "sender", "balance": 500.0, "currency": "PLN"}
    acc2 = {"accountNumber": "TEST-002", "ownerName" : "receiver", "balance": 300.0, "currency": "PLN"}

    try:
        requests.post(ACCOUNT_API_URL, json=acc1)
        requests.post(ACCOUNT_API_URL, json=acc2)
        logging.info("Test accounts created successfully")

        transfer = {
            "fromAccount": "TEST-001",
            "toAccount": "TEST-002",
            "amount": 100.0,
            "title": "Smoke test transfer",
            "currency": "PLN"
        }
        transfer_resp = requests.post(TRANSFER_API_URL, json=transfer)
        if transfer_resp.status_code not in [200,201]:
            logging.error(f"Transfer rejected. HTTP Status: {transfer_resp.status_code}")
            sys.exit(1)
        logging.info("Transfer executed successfully")

        transfer_id = transfer_resp.json().get("id")

        if transfer_id:
            verify_resp = requests.get(f"{TRANSFER_API_URL}/{transfer_id}")
            if verify_resp.status_code == 200:
                logging.info(f"Transfer {transfer_id} verified in the database")
            else:
                logging.error(f"Failed to retrieve transfer {transfer_id}. HTTP Status: {verify_resp.status_code}")
                sys.exit(1)
        else:
            logging.error("Transfer response did not contain an ID")
            sys.exit(1)

        logging.info("All smoke tests passed successfully")

    except requests.exceptions.RequestException as e:
        logging.error(f"Connection failed: {e}")
        sys.exit(1)


if __name__ == "__main__":
    test_critical_path()

