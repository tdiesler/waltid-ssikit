package id.walt.auditor

import id.walt.credentials.w3c.VerifiableCredential
import id.walt.credentials.w3c.VerifiablePresentation
import id.walt.credentials.w3c.toVerifiableCredential
import id.walt.servicematrix.ServiceProvider
import id.walt.services.WaltIdService
import mu.KotlinLogging


private val log = KotlinLogging.logger {}


abstract class Auditor : WaltIdService() {
    override val implementation: Auditor get() = serviceImplementation()

    protected fun allAccepted(policyResults: Map<String, Boolean>) = policyResults.values.all { it }

    open fun verify(vc: VerifiableCredential, policies: List<VerificationPolicy>): VerificationResult =
        implementation.verify(vc, policies)

    open fun verify(vcJson: String, policies: List<VerificationPolicy>): VerificationResult =
        implementation.verify(vcJson, policies)

    companion object : ServiceProvider {
        override fun getService() = object : Auditor() {}
        override fun defaultImplementation() = WaltIdAuditor()
    }
}

class WaltIdAuditor : Auditor() {
    override fun verify(vcJson: String, policies: List<VerificationPolicy>): VerificationResult {
        val vc = vcJson.toVerifiableCredential()
        return verify(vc, policies)
    }

    override fun verify(vc: VerifiableCredential, policies: List<VerificationPolicy>): VerificationResult {

        val policyResults = policies
            .associateBy(keySelector = VerificationPolicy::id) { policy ->
                log.debug { "Verifying vc with ${policy.id} ..." }

                policy.verify(vc) && when (vc) {
                    is VerifiablePresentation -> vc.verifiableCredential?.all { cred ->
                        log.debug { "Verifying ${cred.type.last()} in VP with ${policy.id}..." }
                        policy.verify(cred)
                    } ?: true

                    else -> true
                }
            }

        return VerificationResult(allAccepted(policyResults), policyResults)
    }
}
