package id.walt.services.key

import com.nimbusds.jose.jwk.*
import id.walt.crypto.Key
import id.walt.crypto.KeyAlgorithm
import id.walt.crypto.KeyId
import id.walt.servicematrix.ServiceProvider
import id.walt.services.WaltIdService
import id.walt.services.keystore.KeyType
import org.bouncycastle.jce.ECNamedCurveTable
import org.web3j.crypto.ECDSASignature

enum class KeyFormat {
    JWK,
    PEM
}

abstract class KeyService : WaltIdService() {
    override val implementation get() = serviceImplementation<KeyService>()


    open fun generate(keyAlgorithm: KeyAlgorithm): KeyId = implementation.generate(keyAlgorithm)

    open fun addAlias(keyId: KeyId, alias: String): Unit = implementation.addAlias(keyId, alias)

    open fun load(keyAlias: String): Key = implementation.load(keyAlias)
    open fun load(keyAlias: String, keyType: KeyType = KeyType.PUBLIC): Key = implementation.load(keyAlias, keyType)

    open fun export(
        keyAlias: String,
        format: KeyFormat = KeyFormat.JWK,
        exportKeyType: KeyType = KeyType.PUBLIC
    ): String =
        implementation.export(keyAlias, format, exportKeyType)

    open fun importKey(keyStr: String): KeyId = implementation.importKey(keyStr)

    open fun toJwk(keyAlias: String, keyType: KeyType = KeyType.PUBLIC, jwkKeyId: String? = null): JWK =
        implementation.toJwk(keyAlias, keyType, jwkKeyId)

    open fun toPem(keyAlias: String, keyType: KeyType): String = implementation.toPem(keyAlias, keyType)

    open fun toSecp256Jwk(key: Key, curve: Curve, jwkKeyId: String? = null): ECKey =
        implementation.toSecp256Jwk(key, curve, jwkKeyId)

    open fun toEd25519Jwk(key: Key, jwkKeyId: String? = null): OctetKeyPair = implementation.toEd25519Jwk(key, jwkKeyId)

    open fun toRsaJwk(key: Key, jwkKeyId: String?): RSAKey = implementation.toRsaJwk(key, jwkKeyId)

    open fun getEthereumAddress(keyAlias: String): String = implementation.getEthereumAddress(keyAlias)

    open fun getRecoveryId(keyAlias: String, data: ByteArray, sig: ECDSASignature): Int =
        implementation.getRecoveryId(keyAlias, data, sig)

    open fun listKeys(): List<Key> = implementation.listKeys()

    open fun delete(alias: String): Unit = implementation.delete(alias)

    open fun hasKey(alias: String): Boolean = implementation.hasKey(alias)

    // TODO: consider deprecated methods below

    @Deprecated(message = "outdated")
    open fun getSupportedCurveNames(): List<String> {
        val ecNames = ArrayList<String>()
        for (name in ECNamedCurveTable.getNames()) {
            ecNames.add(name.toString())
        }
        return ecNames
    }

    companion object : ServiceProvider {
        override fun getService() = object : KeyService() {}
        override fun defaultImplementation() = WaltIdKeyService()
    }
}
