package com.test.gcp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class HelloHttpFunction implements HttpFunction {
	private static final Logger logger = Logger.getLogger(HelloHttpFunction.class.getName());
	private static final Gson gson = new Gson();
	private static final Decoder BASE64_DECODER = Base64.getDecoder();

	public void service(final HttpRequest request, final HttpResponse response) throws Exception {
		StringBuilder logSummary = new StringBuilder();

		// Default values avoid null issues (with switch/case) and exceptions from get()
		// (optionals)
		String contentType = request.getContentType().orElse("");

		switch (contentType) {
		case "application/json":
			try {
				JsonObject requestBody = gson.fromJson(request.getReader(), JsonObject.class);
	
				// Single entry in call is under key "message"
				JsonElement msg = requestBody.get("message");
				requestBody = gson.fromJson(msg, JsonObject.class);
	
				//
				// message is JSON and has keys - attributes, data, messageId, message_id, publishTime, publish_time
				//
				// logger.info("Recieved: '" + jsonPrintableString(requestBody) + "'");
				logSummary.append("Key 'message' - JSON Content: '" + jsonPrintableString(requestBody) + "'");
	
				// Pulling 'data' field out of 'message' JSON
				JsonElement dataField = requestBody.get("data");
				// String JsonPrimitive
				JsonPrimitive gcsAuditEntry_Base64 = gson.fromJson(dataField, JsonPrimitive.class);
	
				// Field is provided as Base64 encoded string, this converts to UTF-8 string
				String gcsAuditEntry_Decoded_toJSON = new String(BASE64_DECODER.decode(gcsAuditEntry_Base64.getAsString()),
						StandardCharsets.UTF_8);
	
				//logSummary.append("Key 'data' - Decoded String Content: '" + gcsAuditEntry_Decoded_toJSON + "'\n");
	
				// Deserialize JSON string
				//
				// JSON has keys - insertId, logName, protoPayload, receiveTimestamp, resource
				//
				JsonObject eventJSONBody = gson.fromJson(gcsAuditEntry_Decoded_toJSON, JsonObject.class);
	
				// Print key value pairs for JSON string
				//logSummary.append("\nGCS Audit Log Event - JSON Content: '" + jsonPrintableString(eventJSONBody) + "'");
				
				// Pull original GCS Audit Log JSON event
				JsonElement protoPayloadJSON = eventJSONBody.get("protoPayload");
				
				// Deserialize original GCS Audit Message
				JsonObject gcsAuditEventMessage = gson.fromJson(protoPayloadJSON, JsonObject.class);
	//			logSummary.append("\nGCS Audit Log Event - Content: '" + jsonPrintableString(gcsAuditEventMessage) + "'");
				
				// Metadata field with audit original & extended audit log headers
				String metadataJSON = gcsAuditEventMessage.get("metadata").toString();
				JsonObject auditEventJSONBody = gson.fromJson(metadataJSON, JsonObject.class);
				
				//logSummary.append("\nGCS Audit Log Event / Key 'metadata' - Content: '" + jsonPrintableString(auditEventJSONBody) + "'");
				
				JsonElement auditContext = auditEventJSONBody.get("audit_context");
				JsonObject gcsAuditMetadataFields = gson.fromJson(auditContext, JsonObject.class);
				JsonElement auditInfoJSON = gcsAuditMetadataFields.get("audit_info");
	
				JsonObject auditInfoObject = gson.fromJson(auditInfoJSON, JsonObject.class);			
				//logSummary.append("\nGCS Audit Log Event / Key 'audit_info' - Content: '" + jsonPrintableString(auditInfoObject) + "'");
				
				//
				// NOTE: these should be updated for specific additional audit headers.
				//
				// Expected additional audit headers are:
				//		x-goog-custom-audit-correlation-id
				//		x-goog-custom-audit-signed-by
				// 		x-goog-custom-audit-source
				logSummary.append("\nOriginal Signed URL call - GCS Audit Log Entry's Extended Audit Info Headers: \n");
				logSummary.append("\tx-goog-custom-audit-correlation-id: " + auditInfoObject.get("x-goog-custom-audit-correlation-id"));
				logSummary.append("\n\tx-goog-custom-audit-signed-by" + auditInfoObject.get("x-goog-custom-audit-signed-by"));
				logSummary.append("\n\tx-goog-custom-audit-source" + auditInfoObject.get("x-goog-custom-audit-source"));
				
				// Print multi-stage decoding of request body
				logger.info(logSummary.toString());
			} catch(Exception ex) {
				//
				// NOTE: for troubleshooting, this can be uncommented.
				//
//				StringWriter stackTrace = new StringWriter();
//				ex.printStackTrace(new PrintWriter(stackTrace));
//				logger.warning("Exception during processing.\n" + stackTrace.toString());
			} finally {
				//
				// NOTE: for troubleshooting, this can be uncommented.
				//
				// This gets rather noisy.
//				logger.info(
//						"Failed to parse application/json type message all the way to 'audit_info', including parsed part: \n"
//								+ logSummary.toString());
			}
			break;

		default:
			logger.info("Non-JSON Request");
			String requestText = request.getReader().lines().collect(Collectors.joining("\n"));
			logger.info("Text Body: '" + requestText + "'");
			break;
		}

		response.getWriter().write("OK");
	}

	private String jsonPrintableString(JsonObject jsonObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");

		for (Entry<String, JsonElement> curJsonEntry : jsonObject.entrySet()) {
			sb.append("Key='" + curJsonEntry.getKey().toString() + "'");
			sb.append(" Value='" + curJsonEntry.getValue().toString() + "'");
			sb.append("\n");
		}

		sb.append("\n");

		return sb.toString();
	}
}
