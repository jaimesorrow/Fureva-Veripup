package com.fureva.veripup.app

import com.fureva.veripup.integration.MockAkcVerificationProvider
import com.fureva.veripup.integration.MockClinicVerificationProvider
import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.OnboardingAgreementType
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.service.BreederOnboardingService
import com.fureva.veripup.service.VerificationService
import com.fureva.veripup.workflow.BreederWorkflowSnapshot
import com.fureva.veripup.workflow.InMemoryBreederOnboardingRepository
import com.fureva.veripup.workflow.InMemoryBreederProfileRepository
import com.fureva.veripup.workflow.InMemoryVerificationReviewRepository
import com.fureva.veripup.workflow.VerificationReviewRecord
import com.fureva.veripup.workflow.VerificationWorkflowService
import com.fureva.veripup.workflow.WorkflowNotFoundException
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.Instant

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val server = createServer(port)
    println("Fureva VeriPup demo server running at http://localhost:$port")
    server.start()
}

fun createServer(port: Int): HttpServer {
    val workflow = VerificationWorkflowService(
        breederProfiles = InMemoryBreederProfileRepository(),
        onboardingSubmissions = InMemoryBreederOnboardingRepository(),
        verificationReviews = InMemoryVerificationReviewRepository(),
        onboardingService = BreederOnboardingService(),
        verificationService = VerificationService(
            clinicVerificationProvider = MockClinicVerificationProvider(),
            akcVerificationProvider = MockAkcVerificationProvider()
        )
    )

    val server = HttpServer.create(InetSocketAddress(port), 0)
    server.createContext("/") { exchange ->
        runCatching { route(exchange, workflow) }
            .onFailure { error ->
                when (error) {
                    is WorkflowNotFoundException ->
                        sendText(exchange, error.message ?: "Not found", 404, "text/plain; charset=utf-8")
                    is IllegalArgumentException ->
                        sendText(exchange, error.message ?: "Bad request", 400, "text/plain; charset=utf-8")
                    else ->
                        sendText(exchange, error.message ?: "Unexpected error", 500, "text/plain; charset=utf-8")
                }
            }
    }
    return server
}

private fun route(exchange: HttpExchange, workflow: VerificationWorkflowService) {
    val path = exchange.requestURI.path.removeSuffix("/").ifBlank { "/" }
    when {
        exchange.requestMethod == "GET" && path == "/" ->
            sendHtml(exchange, renderHomePage(workflow))

        exchange.requestMethod == "POST" && path == "/breeders" ->
            createBreeder(exchange, workflow)

        exchange.requestMethod == "GET" && path.startsWith("/breeders/") && path.count { it == '/' } == 2 ->
            showBreeder(exchange, workflow, path.removePrefix("/breeders/"))

        exchange.requestMethod == "POST" && path.startsWith("/breeders/") && path.endsWith("/onboarding") ->
            submitOnboarding(exchange, workflow, path.removePrefix("/breeders/").removeSuffix("/onboarding"))

        exchange.requestMethod == "POST" && path.startsWith("/breeders/") && path.endsWith("/verification") ->
            submitVerification(exchange, workflow, path.removePrefix("/breeders/").removeSuffix("/verification"))

        exchange.requestMethod == "GET" && path == "/admin/verifications" ->
            sendHtml(exchange, renderAdminQueuePage(workflow))

        exchange.requestMethod == "POST" && path.startsWith("/admin/verifications/") && path.endsWith("/approve") ->
            reviewVerification(exchange, workflow, extractActionId(path, "/admin/verifications/", "/approve"), true)

        exchange.requestMethod == "POST" && path.startsWith("/admin/verifications/") && path.endsWith("/reject") ->
            reviewVerification(exchange, workflow, extractActionId(path, "/admin/verifications/", "/reject"), false)

        exchange.requestMethod == "GET" && path == "/api/breeders" ->
            sendJson(exchange, breedersJson(workflow))

        exchange.requestMethod == "GET" && path.startsWith("/api/breeders/") && path.count { it == '/' } == 3 ->
            sendJson(exchange, breederSnapshotJson(workflow, path.removePrefix("/api/breeders/")))

        exchange.requestMethod == "GET" && path == "/api/verifications/queue" ->
            sendJson(exchange, queueJson(workflow.listVerificationQueue()))

        else -> sendText(exchange, "Not found", 404, "text/plain; charset=utf-8")
    }
}

private fun createBreeder(exchange: HttpExchange, workflow: VerificationWorkflowService) {
    val form = parseFormBody(exchange)
    val breeder = workflow.registerBreeder(
        BreederProfile(
            id = form.required("id"),
            name = form.required("name"),
            stateCode = form.required("stateCode"),
            city = form.required("city")
        )
    )
    redirect(exchange, "/breeders/${breeder.id}")
}

private fun showBreeder(exchange: HttpExchange, workflow: VerificationWorkflowService, breederId: String) {
    val snapshot = workflow.getWorkflowSnapshot(breederId)
        ?: return sendText(exchange, "Breeder not found", 404, "text/plain; charset=utf-8")
    sendHtml(exchange, renderBreederPage(snapshot))
}

private fun submitOnboarding(exchange: HttpExchange, workflow: VerificationWorkflowService, breederId: String) {
    val form = parseFormBody(exchange)
    workflow.submitOnboarding(
        BreederOnboardingSubmission(
            breederId = breederId,
            governmentIdUploaded = form.boolean("governmentIdUploaded"),
            photoHoldingGovernmentIdUploaded = form.boolean("photoHoldingGovernmentIdUploaded"),
            vetRecordsUploaded = form.boolean("vetRecordsUploaded"),
            vetRecordsCoverBreedingDogs = form.boolean("vetRecordsCoverBreedingDogs"),
            acceptedAgreements = OnboardingAgreementType.entries.filterTo(mutableSetOf()) { form.boolean("agreement_${it.name}") },
            signedAt = form.instant("signedAt")
        )
    )
    redirect(exchange, "/breeders/$breederId")
}

private fun submitVerification(exchange: HttpExchange, workflow: VerificationWorkflowService, breederId: String) {
    val form = parseFormBody(exchange)
    workflow.submitVerification(
        VerificationSubmission(
            breederId = breederId,
            governmentIdReadable = form.boolean("governmentIdReadable"),
            idMatchesSignup = form.boolean("idMatchesSignup"),
            firstLiveVideoPassedDeepfake = form.boolean("firstLiveVideoPassedDeepfake"),
            vetDocsUploaded = form.boolean("vetDocsUploaded"),
            vetDocsMatchBreed = form.boolean("vetDocsMatchBreed"),
            optionalAkcNumber = form.optional("optionalAkcNumber"),
            secondLiveVideoPassedDeepfake = form.boolean("secondLiveVideoPassedDeepfake"),
            clinicPhone = form.required("clinicPhone"),
            roiSigned = form.boolean("roiSigned")
        )
    )
    redirect(exchange, "/breeders/$breederId")
}

private fun reviewVerification(
    exchange: HttpExchange,
    workflow: VerificationWorkflowService,
    recordId: String,
    approved: Boolean
) {
    val form = parseFormBody(exchange)
    workflow.reviewVerification(
        recordId = recordId,
        approved = approved,
        reviewNotes = form.optional("reviewNotes")
    )
    redirect(exchange, "/admin/verifications")
}

private fun renderHomePage(workflow: VerificationWorkflowService): String {
    val breeders = workflow.listBreeders()
    val breederLinks = if (breeders.isEmpty()) {
        "<p>No breeders registered yet.</p>"
    } else {
        "<ul>${breeders.joinToString("") { "<li><a href=\"/breeders/${html(it.id)}\">${html(it.name)}</a> (${html(it.id)})</li>" }}</ul>"
    }

    return """
        <html>
        <head><title>Fureva VeriPup Demo</title></head>
        <body>
            <h1>Fureva VeriPup workflow demo</h1>
            <p>This runnable layer adds an in-memory workflow for breeder onboarding, verification submission, and admin review.</p>
            <p><a href="/admin/verifications">Admin verification dashboard</a></p>

            <h2>Register breeder</h2>
            <form method="post" action="/breeders">
                <label>Breeder ID <input name="id" required /></label><br/>
                <label>Name <input name="name" required /></label><br/>
                <label>State code <input name="stateCode" maxlength="2" required /></label><br/>
                <label>City <input name="city" required /></label><br/>
                <button type="submit">Create breeder</button>
            </form>

            <h2>Breeders</h2>
            $breederLinks

            <h2>API endpoints</h2>
            <ul>
                <li><a href="/api/breeders">/api/breeders</a></li>
                <li><a href="/api/verifications/queue">/api/verifications/queue</a></li>
            </ul>
        </body>
        </html>
    """.trimIndent()
}

private fun renderBreederPage(snapshot: BreederWorkflowSnapshot): String {
    val onboardingStatus = snapshot.onboardingStatus
    val verification = snapshot.verificationRecord
    val agreementCheckboxes = OnboardingAgreementType.entries.joinToString("<br/>") { agreement ->
        val checked = snapshot.onboardingSubmission?.acceptedAgreements?.contains(agreement) == true
        """<label><input type="checkbox" name="agreement_${agreement.name}" ${checked.checked()} /> ${html(agreement.name)}</label>"""
    }

    val missingList = onboardingStatus?.missingRequirements?.takeIf { it.isNotEmpty() }?.joinToString("") {
        "<li>${html(it)}</li>"
    } ?: ""

    return """
        <html>
        <head><title>${html(snapshot.profile.name)}</title></head>
        <body>
            <p><a href="/">Back to home</a> | <a href="/admin/verifications">Admin verification dashboard</a></p>
            <h1>${html(snapshot.profile.name)} (${html(snapshot.profile.id)})</h1>
            <p>Location: ${html(snapshot.profile.city)}, ${html(snapshot.profile.stateCode.uppercase())}</p>
            <p>Verified: ${snapshot.profile.verifiedStatus}</p>

            <h2>Onboarding status</h2>
            <p>Ready for verification: ${onboardingStatus?.readyForVerification ?: false}</p>
            ${if (missingList.isNotEmpty()) "<ul>$missingList</ul>" else "<p>No missing onboarding requirements.</p>"}

            <h2>Submit onboarding</h2>
            <form method="post" action="/breeders/${html(snapshot.profile.id)}/onboarding">
                <label><input type="checkbox" name="governmentIdUploaded" ${snapshot.onboardingSubmission?.governmentIdUploaded.checked()} /> Government ID uploaded</label><br/>
                <label><input type="checkbox" name="photoHoldingGovernmentIdUploaded" ${snapshot.onboardingSubmission?.photoHoldingGovernmentIdUploaded.checked()} /> Photo holding ID uploaded</label><br/>
                <label><input type="checkbox" name="vetRecordsUploaded" ${snapshot.onboardingSubmission?.vetRecordsUploaded.checked()} /> Vet records uploaded</label><br/>
                <label><input type="checkbox" name="vetRecordsCoverBreedingDogs" ${snapshot.onboardingSubmission?.vetRecordsCoverBreedingDogs.checked()} /> Vet records cover breeding dogs</label><br/>
                <label>Signed at (ISO-8601) <input name="signedAt" value="${html(snapshot.onboardingSubmission?.signedAt?.toString().orEmpty())}" placeholder="2026-01-01T00:00:00Z" /></label><br/>
                <fieldset>
                    <legend>Required agreements</legend>
                    $agreementCheckboxes
                </fieldset>
                <button type="submit">Save onboarding</button>
            </form>

            <h2>Submit verification</h2>
            <form method="post" action="/breeders/${html(snapshot.profile.id)}/verification">
                <label><input type="checkbox" name="governmentIdReadable" /> Government ID readable</label><br/>
                <label><input type="checkbox" name="idMatchesSignup" /> ID matches signup</label><br/>
                <label><input type="checkbox" name="firstLiveVideoPassedDeepfake" /> First live video passed</label><br/>
                <label><input type="checkbox" name="vetDocsUploaded" /> Vet docs uploaded</label><br/>
                <label><input type="checkbox" name="vetDocsMatchBreed" /> Vet docs match breed</label><br/>
                <label><input type="checkbox" name="secondLiveVideoPassedDeepfake" /> Second live video passed</label><br/>
                <label><input type="checkbox" name="roiSigned" /> ROI signed</label><br/>
                <label>Clinic phone <input name="clinicPhone" required /></label><br/>
                <label>Optional AKC number <input name="optionalAkcNumber" placeholder="AKC-12345" /></label><br/>
                <button type="submit">Submit verification</button>
            </form>

            <h2>Latest verification</h2>
            ${renderVerificationSummary(verification)}
            <p><a href="/api/breeders/${html(snapshot.profile.id)}">View breeder JSON</a></p>
        </body>
        </html>
    """.trimIndent()
}

private fun renderVerificationSummary(record: VerificationReviewRecord?): String {
    if (record == null) return "<p>No verification submitted yet.</p>"
    return """
        <ul>
            <li>Status: ${html(record.status.name)}</li>
            <li>Policy approved: ${record.policyApproved}</li>
            <li>Submitted at: ${html(record.submittedAt.toString())}</li>
            <li>Reviewed at: ${html(record.reviewedAt?.toString().orEmpty())}</li>
            <li>Notes: ${html(record.reviewNotes.orEmpty())}</li>
        </ul>
    """.trimIndent()
}

private fun renderAdminQueuePage(workflow: VerificationWorkflowService): String {
    val pending = workflow.listVerificationQueue()
    val allRecords = workflow.listVerificationRecords()

    val pendingHtml = if (pending.isEmpty()) {
        "<p>No verification records are waiting for review.</p>"
    } else {
        pending.joinToString("") { record ->
            """
            <li>
                <strong>${html(record.breederId)}</strong> submitted ${html(record.submittedAt.toString())}
                <form method="post" action="/admin/verifications/${html(record.id)}/approve">
                    <input name="reviewNotes" placeholder="Approval notes" />
                    <button type="submit">Approve</button>
                </form>
                <form method="post" action="/admin/verifications/${html(record.id)}/reject">
                    <input name="reviewNotes" placeholder="Rejection notes" />
                    <button type="submit">Reject</button>
                </form>
            </li>
            """.trimIndent()
        }
    }

    val allHtml = if (allRecords.isEmpty()) {
        "<p>No verification records yet.</p>"
    } else {
        "<ul>${allRecords.joinToString("") { "<li>${html(it.breederId)} — ${html(it.status.name)} — ${html(it.reviewNotes.orEmpty())}</li>" }}</ul>"
    }

    return """
        <html>
        <head><title>Admin verification dashboard</title></head>
        <body>
            <p><a href="/">Back to home</a></p>
            <h1>Admin verification dashboard</h1>
            <h2>Pending review</h2>
            <ul>$pendingHtml</ul>

            <h2>All verification records</h2>
            $allHtml
            <p><a href="/api/verifications/queue">View queue JSON</a></p>
        </body>
        </html>
    """.trimIndent()
}

private fun breedersJson(workflow: VerificationWorkflowService): String =
    workflow.listBreeders().joinToJsonArray { breeder ->
        """
        {
          "id": ${json(breeder.id)},
          "name": ${json(breeder.name)},
          "stateCode": ${json(breeder.stateCode)},
          "city": ${json(breeder.city)},
          "verifiedStatus": ${breeder.verifiedStatus}
        }
        """.trimIndent()
    }

private fun breederSnapshotJson(workflow: VerificationWorkflowService, breederId: String): String {
    val snapshot = workflow.getWorkflowSnapshot(breederId)
        ?: return """{"error":"Breeder not found"}"""

    val fields = mutableListOf(
        """
        "profile": {
          "id": ${json(snapshot.profile.id)},
          "name": ${json(snapshot.profile.name)},
          "stateCode": ${json(snapshot.profile.stateCode)},
          "city": ${json(snapshot.profile.city)},
          "verifiedStatus": ${snapshot.profile.verifiedStatus}
        }
        """.trimIndent()
    )
    snapshot.onboardingStatus?.let {
        fields +=
            """
            "onboardingStatus": {
              "readyForVerification": ${it.readyForVerification},
              "missingRequirements": ${it.missingRequirements.joinToJsonArray(::json)}
            }
            """.trimIndent()
    }
    fields += snapshot.verificationRecord?.let {
        """
        "verificationRecord": {
          "id": ${json(it.id)},
          "status": ${json(it.status.name)},
          "policyApproved": ${it.policyApproved},
          "submittedAt": ${json(it.submittedAt.toString())},
          "reviewedAt": ${it.reviewedAt?.let { reviewedAt -> json(reviewedAt.toString()) } ?: "null"},
          "reviewNotes": ${it.reviewNotes?.let(::json) ?: "null"}
        }
        """.trimIndent()
    } ?: """"verificationRecord": null"""

    return """
        {
          ${fields.joinToString(",\n  ")}
        }
    """.trimIndent()
}

private fun queueJson(records: List<VerificationReviewRecord>): String =
    records.joinToJsonArray { record ->
        """
        {
          "id": ${json(record.id)},
          "breederId": ${json(record.breederId)},
          "status": ${json(record.status.name)},
          "policyApproved": ${record.policyApproved},
          "submittedAt": ${json(record.submittedAt.toString())},
          "reviewNotes": ${record.reviewNotes?.let(::json) ?: "null"}
        }
        """.trimIndent()
    }

private fun parseFormBody(exchange: HttpExchange): FormData {
    val rawBody = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).readText()
    if (rawBody.isBlank()) return FormData(emptyMap())
    val values = rawBody.split("&")
        .filter { it.isNotBlank() }
        .associate { part ->
            val pieces = part.split("=", limit = 2)
            val key = URLDecoder.decode(pieces[0], StandardCharsets.UTF_8)
            val value = URLDecoder.decode(pieces.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            key to value
        }
    return FormData(values)
}

private fun sendHtml(exchange: HttpExchange, body: String) =
    sendText(exchange, body, 200, "text/html; charset=utf-8")

private fun sendJson(exchange: HttpExchange, body: String) =
    sendText(exchange, body, 200, "application/json; charset=utf-8")

private fun sendText(exchange: HttpExchange, body: String, status: Int, contentType: String) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    exchange.responseHeaders.add("Content-Type", contentType)
    exchange.sendResponseHeaders(status, bytes.size.toLong())
    exchange.responseBody.use { it.write(bytes) }
}

private fun redirect(exchange: HttpExchange, location: String) {
    exchange.responseHeaders.add("Location", location)
    exchange.sendResponseHeaders(302, -1)
    exchange.close()
}

private fun extractActionId(path: String, prefix: String, suffix: String): String {
    val extracted = path.removePrefix(prefix).removeSuffix(suffix)
    require(extracted.isNotBlank() && !extracted.contains('/')) { "Invalid action target." }
    return extracted
}

private fun json(value: String): String =
    buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                in '\u0000'..'\u001F' -> append("\\u%04x".format(char.code))
                else -> append(char)
            }
        }
        append('"')
    }

private fun html(value: String): String =
    value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

private fun <T> List<T>.joinToJsonArray(render: (T) -> String): String =
    joinToString(prefix = "[", postfix = "]", separator = ",") { render(it) }

private fun Boolean?.checked(): String = if (this == true) "checked" else ""

private class FormData(private val values: Map<String, String>) {
    fun required(key: String): String =
        values[key]?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Missing required field '$key'.")

    fun optional(key: String): String? = values[key]?.trim()?.takeIf { it.isNotEmpty() }

    fun boolean(key: String): Boolean = values[key] != null

    fun instant(key: String): Instant? = optional(key)?.let(Instant::parse)
}
