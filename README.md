# GCP_CRF_Print_GCS_Audit_Log_Entries

## App Purpose
Basic Cloud Run Functions app to parse JSON messages down to the extended audit fields added in GCS Signed URLs.

## Setup:
    
1. Create Pub/Sub topic for message queuing.

2. Create log sink with filter -  ``resource.type="gcs_bucket" protoPayload.methodName="storage.objects.get" protoPayload.serviceName="storage.googleapis.com" `` pointing to the above Pub/Sub topic.
	
3. Create Cloud Run Function with this code (to print GCS Audit Log events).

4. Create Pub/Sub push subscription to call to the Cloud Run Function with GCS Audit log entries (with or without filter).

Sanitized GCS Signed URL - to generate logs below:

    curl -H "x-goog-custom-audit-source: com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1" -H "x-goog-custom-audit-signed-by: sa-gcs-reader@<project-id>.iam.gserviceaccount.com" -H "X-Goog-Log-Me: header not included in logging" -H "x-goog-custom-audit-correlation-id: c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b" "https://storage.googleapis.com/<gcs-bucket-id>/<object-name>?Consumer-Name=client_asdasdasd_1234&Extra-Log-Info=some%20thing%20for%20query%20log%20discovery&Generator-Service=com.test.gcp.GCS_Signed_URL_With_Addtional_Headers%2F0.1&X-Goog-Algorithm=GOOG4-RSA-SHA256&X-Goog-Credential=sa-gcs-reader%40<project-id>.iam.gserviceaccount.com%2F20250303%2Fauto%2Fstorage%2Fgoog4_request&X-Goog-Date=20250303T014330Z&X-Goog-Expires=900&X-Goog-SignedHeaders=host%3Bx-goog-custom-audit-correlation-id%3Bx-goog-custom-audit-signed-by%3Bx-goog-custom-audit-source%3Bx-goog-log-me&X-Goog-Signature=3b60e35a6cc5076a0b5b699991266ed6e16412ac746cd7727c27e5021de6d363a00b1e9c7ef2822a8f6b03b715e5aa2d8504a3c7fec1b337a983c7c3b6047625e96541e979e0d34629999a6bd85f6c950336e636a653d0a9c918b13a9d1eaf3232e19b43de6330b4d8f8953af8702bcf3053799c7c2e21856d6774eccfa5234ece9471999997d083c13bb4546aacda82060ce3d03693a17bbb289aff58fe5b4c6a09fc7893bc28c1f6e818489c3e81446d34e111a0880e0ce8ab0ce6b9f57bfe340fc9a9374be41bd989bd31610f024e53c9e79a0018ccf710b0acda32da02938257a59d4b51ae7b567f291ffe3b63f99a383142e2308d01ee999993d12b8439"

Sanitized deserialized log output:

```json
message: "Key 'message' - JSON Content: '
Key='attributes' Value='{"logging.googleapis.com/timestamp":"2025-03-03T01:44:06.143517192Z"}'
Key='data' Value='"eyJpbnNlcnRJZCI6IjFuNXY0eXFlcmlrOTkiLCJsb2dOYW1lIjoicHJvamVjdHMvPHByb2plY3QtaWQ+L2xvZ3MvY2xvdWRhdWRpdC5nb29nbGVhcGlzLmNvbSUyRmRhdGFfYWNjZXNzIiwicHJvdG9QYXlsb2FkIjp7IkB0eXBlIjoidHlwZS5nb29nbGVhcGlzLmNvbS9nb29nbGUuY2xvdWQuYXVkaXQuQXVkaXRMb2ciLCJhdXRoZW50aWNhdGlvbkluZm8iOnsicHJpbmNpcGFsRW1haWwiOiJzYS1nY3MtcmVhZGVyQDxwcm9qZWN0LWlkPi5pYW0uZ3NlcnZpY2VhY2NvdW50LmNvbSJ9LCJhdXRob3JpemF0aW9uSW5mbyI6W3siZ3JhbnRlZCI6dHJ1ZSwicGVybWlzc2lvbiI6InN0b3JhZ2Uub2JqZWN0cy5nZXQiLCJyZXNvdXJjZSI6InByb2plY3RzL18vYnVja2V0cy88Z2NzLWJ1Y2tldC1pZD4vb2JqZWN0cy88b2JqZWN0LW5hbWU+IiwicmVzb3VyY2VBdHRyaWJ1dGVzIjp7fX1dLCJtZXRhZGF0YSI6eyJhdWRpdF9jb250ZXh0Ijp7ImFwcF9jb250ZXh0IjoiRVhURVJOQUwiLCJhdWRpdF9pbmZvIjp7IngtZ29vZy1jdXN0b20tYXVkaXQtY29ycmVsYXRpb24taWQiOiJjM2NlMTJmYy0wY2U1LTRmZjQtOTVjNi1mZWU5YjQ2NmE3MWIiLCJ4LWdvb2ctY3VzdG9tLWF1ZGl0LXNpZ25lZC1ieSI6InNhLWdjcy1yZWFkZXJAPHByb2plY3QtaWQ+LmlhbS5nc2VydmljZWFjY291bnQuY29tIiwieC1nb29nLWN1c3RvbS1hdWRpdC1zb3VyY2UiOiJjb20udGVzdC5nY3AuR0NTX1NpZ25lZF9VUkxfV2l0aF9BZGR0aW9uYWxfSGVhZGVycy8wLjEifX19LCJtZXRob2ROYW1lIjoic3RvcmFnZS5vYmplY3RzLmdldCIsInJlcXVlc3RNZXRhZGF0YSI6eyJjYWxsZXJJcCI6IjkuOS45LjkiLCJjYWxsZXJTdXBwbGllZFVzZXJBZ2VudCI6ImN1cmwvOC4wLjEsZ3ppcChnZmUpIiwiZGVzdGluYXRpb25BdHRyaWJ1dGVzIjp7fSwicmVxdWVzdEF0dHJpYnV0ZXMiOnsiYXV0aCI6e30sInRpbWUiOiIyMDI1LTAzLTAzVDAxOjQ0OjA2LjE1MzQwNDc1MVoifX0sInJlc291cmNlTG9jYXRpb24iOnsiY3VycmVudExvY2F0aW9ucyI6WyJ1cyJdfSwicmVzb3VyY2VOYW1lIjoicHJvamVjdHMvXy9idWNrZXRzLzxnY3MtYnVja2V0LWlkPi9vYmplY3RzLzxvYmplY3QtbmFtZT4iLCJzZXJ2aWNlTmFtZSI6InN0b3JhZ2UuZ29vZ2xlYXBpcy5jb20iLCJzdGF0dXMiOnt9fSwicmVjZWl2ZVRpbWVzdGFtcCI6IjIwMjUtMDMtMDNUMDE6NDQ6MDYuNDI1MTEzNjExWiIsInJlc291cmNlIjp7ImxhYmVscyI6eyJidWNrZXRfbmFtZSI6IjxnY3MtYnVja2V0LWlkPiIsImxvY2F0aW9uIjoidXMiLCJwcm9qZWN0X2lkIjoiPHByb2plY3QtaWQ+In0sInR5cGUiOiJnY3NfYnVja2V0In0sInNldmVyaXR5IjoiSU5GTyIsInRpbWVzdGFtcCI6IjIwMjUtMDMtMDNUMDE6NDQ6MDYuMTQzNTE3MTkyWiJ9"'
Key='messageId' Value='"14128681004859449"'
Key='message_id' Value='"14128681004859449"'
Key='publishTime' Value='"2025-03-03T01:44:08.016Z"'
Key='publish_time' Value='"2025-03-03T01:44:08.016Z"'

'Key 'data' - Decoded String Content: '{"insertId":"1n5v4yqerik99","logName":"projects/<project-id>/logs/cloudaudit.googleapis.com%2Fdata_access","protoPayload":{"@type":"type.googleapis.com/google.cloud.audit.AuditLog","authenticationInfo":{"principalEmail":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com"},"authorizationInfo":[{"granted":true,"permission":"storage.objects.get","resource":"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>","resourceAttributes":{}}],"metadata":{"audit_context":{"app_context":"EXTERNAL","audit_info":{"x-goog-custom-audit-correlation-id":"c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b","x-goog-custom-audit-signed-by":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com","x-goog-custom-audit-source":"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1"}}},"methodName":"storage.objects.get","requestMetadata":{"callerIp":"9.9.9.9","callerSuppliedUserAgent":"curl/8.0.1,gzip(gfe)","destinationAttributes":{},"requestAttributes":{"auth":{},"time":"2025-03-03T01:44:06.153404751Z"}},"resourceLocation":{"currentLocations":["us"]},"resourceName":"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>","serviceName":"storage.googleapis.com","status":{}},"receiveTimestamp":"2025-03-03T01:44:06.425113611Z","resource":{"labels":{"bucket_name":"<gcs-bucket-id>","location":"us","project_id":"<project-id>"},"type":"gcs_bucket"},"severity":"INFO","timestamp":"2025-03-03T01:44:06.143517192Z"}'

GCS Audit Log Event - JSON Content: '
Key='insertId' Value='"1n5v4yqerik99"'
Key='logName' Value='"projects/<project-id>/logs/cloudaudit.googleapis.com%2Fdata_access"'
Key='protoPayload' Value='{"@type":"type.googleapis.com/google.cloud.audit.AuditLog","authenticationInfo":{"principalEmail":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com"},"authorizationInfo":[{"granted":true,"permission":"storage.objects.get","resource":"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>","resourceAttributes":{}}],"metadata":{"audit_context":{"app_context":"EXTERNAL","audit_info":{"x-goog-custom-audit-correlation-id":"c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b","x-goog-custom-audit-signed-by":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com","x-goog-custom-audit-source":"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1"}}},"methodName":"storage.objects.get","requestMetadata":{"callerIp":"9.9.9.9","callerSuppliedUserAgent":"curl/8.0.1,gzip(gfe)","destinationAttributes":{},"requestAttributes":{"auth":{},"time":"2025-03-03T01:44:06.153404751Z"}},"resourceLocation":{"currentLocations":["us"]},"resourceName":"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>","serviceName":"storage.googleapis.com","status":{}}'
Key='receiveTimestamp' Value='"2025-03-03T01:44:06.425113611Z"'
Key='resource' Value='{"labels":{"bucket_name":"<gcs-bucket-id>","location":"us","project_id":"<project-id>"},"type":"gcs_bucket"}'
Key='severity' Value='"INFO"'
Key='timestamp' Value='"2025-03-03T01:44:06.143517192Z"'

'
GCS Audit Log Event - Content: '
Key='@type' Value='"type.googleapis.com/google.cloud.audit.AuditLog"'
Key='authenticationInfo' Value='{"principalEmail":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com"}'
Key='authorizationInfo' Value='[{"granted":true,"permission":"storage.objects.get","resource":"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>","resourceAttributes":{}}]'
Key='metadata' Value='{"audit_context":{"app_context":"EXTERNAL","audit_info":{"x-goog-custom-audit-correlation-id":"c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b","x-goog-custom-audit-signed-by":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com","x-goog-custom-audit-source":"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1"}}}'
Key='methodName' Value='"storage.objects.get"'
Key='requestMetadata' Value='{"callerIp":"9.9.9.9","callerSuppliedUserAgent":"curl/8.0.1,gzip(gfe)","destinationAttributes":{},"requestAttributes":{"auth":{},"time":"2025-03-03T01:44:06.153404751Z"}}'
Key='resourceLocation' Value='{"currentLocations":["us"]}'
Key='resourceName' Value='"projects/_/buckets/<gcs-bucket-id>/objects/<object-name>"'
Key='serviceName' Value='"storage.googleapis.com"'
Key='status' Value='{}'

'
GCS Audit Log Event / Key 'metadata' - Content: '
Key='audit_context' Value='{"app_context":"EXTERNAL","audit_info":{"x-goog-custom-audit-correlation-id":"c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b","x-goog-custom-audit-signed-by":"sa-gcs-reader@<project-id>.iam.gserviceaccount.com","x-goog-custom-audit-source":"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1"}}'

'
GCS Audit Log Event / Key 'audit_info' - Content: '
Key='x-goog-custom-audit-correlation-id' Value='"c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b"'
Key='x-goog-custom-audit-signed-by' Value='"sa-gcs-reader@<project-id>.iam.gserviceaccount.com"'
Key='x-goog-custom-audit-source' Value='"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1"'

'
Original Signed URL call - GCS Audit Log Entry's Extended Audit Info Headers: 
	x-goog-custom-audit-correlation-id: "c3ce12fc-0ce5-4ff4-95c6-fee9b466a71b"
	x-goog-custom-audit-signed-by"sa-gcs-reader@<project-id>.iam.gserviceaccount.com"
	x-goog-custom-audit-source"com.test.gcp.GCS_Signed_URL_With_Addtional_Headers/0.1""
```