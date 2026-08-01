package com.pravesh.notification.util;

public class EmailTemplates {

	private static String wrapper(String title, String bodyHtml) {
		return """
				<div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
				    <h2 style="color: #2c3e50;">%s</h2>
				    %s
				    <p style="color: #888; font-size: 12px; margin-top: 24px;">— Pravesh Security System</p>
				</div>
				"""
				.formatted(title, bodyHtml);
	}

	public static String passCreated(String residentName, String flatNumber, String visitorName, String uuid,
			String validFrom, String validUntil) {
		String body = """
				<p>Dear %s,</p>
				<p>A visitor pass has been created for your flat <b>%s</b>.</p>
				<table style="width:100%%; border-collapse: collapse; margin: 12px 0;">
				    <tr><td style="padding:4px 0; color:#555;">Visitor Name</td><td><b>%s</b></td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Pass UUID</td><td>%s</td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Valid From</td><td>%s</td></tr>
				    <tr><td style="padding:4px 0; color:#555;">Valid Until</td><td>%s</td></tr>
				</table>
				<p>Scan this QR code at the gate:</p>
				<img src="cid:qrImage" style="width:200px; height:200px;" />
				<p>Share the Pass UUID or QR code with your visitor for entry.</p>
				""".formatted(residentName, flatNumber, visitorName, uuid, validFrom, validUntil);
		return wrapper("Visitor Pass Created", body);
	}

	public static String passRevoked(String residentName, String visitorName, String uuid) {
		String body = """
				<p>Dear %s,</p>
				<p>The visitor pass for <b>%s</b> has been revoked and is no longer valid.</p>
				<p style="color:#888;">Pass UUID: %s</p>
				""".formatted(residentName, visitorName, uuid);
		return wrapper("Visitor Pass Revoked", body);
	}

	public static String visitorEntered(String residentName, String visitorName, String gateName, String enteredAt) {
		String body = """
				<p>Dear %s,</p>
				<p><b>%s</b> entered via <b>%s</b> at %s.</p>
				""".formatted(residentName, visitorName, gateName, enteredAt);
		return wrapper("Visitor Entered", body);
	}

	public static String residentApproved(String residentName, String flatNumber, String appLink) {
		String body = """
				<p>Dear %s,</p>
				<p>Great news — your onboarding request has been <b>approved</b>! You're now a verified resident of flat <b>%s</b>.</p>
				<p>You now have full access to Pravesh — create visitor passes, raise SOS alerts, connect with your community, and more.</p>
				<div style="text-align:center; margin: 24px 0;">
				    <a href="%s" style="background:#2c3e50; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
				        Open Pravesh
				    </a>
				</div>
				<p>Welcome aboard!</p>
				"""
				.formatted(residentName, flatNumber, appLink);
		return wrapper("Welcome to Pravesh 🎉", body);
	}

	public static String societyAdminApproved(String adminName, String societyName, String appLink) {
		String body = """
				<p>Dear %s,</p>
				<p>Congratulations — your society <b>%s</b> has been <b>approved</b> and is now live on Pravesh!</p>
				<p>You now have full administrative access — manage residents, guards, gates, analytics, and every module of your society's platform.</p>
				<div style="text-align:center; margin: 24px 0;">
				    <a href="%s" style="background:#2c3e50; color:#fff; padding:12px 24px; text-decoration:none; border-radius:6px; display:inline-block;">
				        Open Pravesh Admin Dashboard
				    </a>
				</div>
				<p>Welcome to Pravesh!</p>
				"""
				.formatted(adminName, societyName, appLink);
		return wrapper("Welcome to Pravesh 🎉", body);
	}
}