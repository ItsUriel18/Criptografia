import base64
import json
from pathlib import Path

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import ec, utils
from cryptography.hazmat.primitives.kdf.hkdf import HKDF

# =========================================================
# 1. UTILIDADES (Base para todo el equipo)
# =========================================================

def obtener_curva(nombre_curva: str):
    """Mapea el nombre del estándar NIST a la curva correspondiente[cite: 4, 21]."""
    curvas = {
        "P-224": ec.SECP224R1(),
        "P-256": ec.SECP256R1(),
        "P-384": ec.SECP384R1(),
        "P-521": ec.SECP521R1(),
    }
    if nombre_curva not in curvas:
        raise ValueError("Curva no válida. Usa: P-224, P-256, P-384 o P-521.")
    return curvas[nombre_curva]

def obtener_hash(nombre_hash: str):
    """Selecciona la función hash de la familia SHA2[cite: 12, 24]."""
    hashes_disponibles = {
        "sha224": hashes.SHA224(),
        "sha256": hashes.SHA256(),
        "sha384": hashes.SHA384(),
        "sha512": hashes.SHA512(),
    }
    return hashes_disponibles.get(nombre_hash.lower(), hashes.SHA256())

def guardar_base64(ruta_archivo: str, datos_binarios: bytes):
    """Guarda datos en Base64 según los acuerdos[cite: 11, 22]."""
    texto_b64 = base64.b64encode(datos_binarios).decode("utf-8")
    Path(ruta_archivo).write_text(texto_b64, encoding="utf-8")

def leer_base64(ruta_archivo: str) -> bytes:
    """Lee y decodifica archivos en Base64[cite: 11]."""
    texto_b64 = Path(ruta_archivo).read_text(encoding="utf-8").strip()
    return base64.b64decode(texto_b64)

# =========================================================
# 2. FUNCIONES DEL INTEGRANTE 1 (Generación y Firma)
# =========================================================

def generar_llaves_ecdsa(nombre_curva, archivo_privada, archivo_publica):
    curva = obtener_curva(nombre_curva)
    privada = ec.generate_private_key(curva)
    publica = privada.public_key()
    
    # Serialización a DER para cumplir con el estándar 
    priv_der = privada.private_bytes(serialization.Encoding.DER, serialization.PrivateFormat.PKCS8, serialization.NoEncryption())
    pub_der = publica.public_bytes(serialization.Encoding.DER, serialization.PublicFormat.SubjectPublicKeyInfo)
    
    guardar_base64(archivo_privada, priv_der)
    guardar_base64(archivo_publica, pub_der)
    print(f"\n✅ Llaves guardadas en {archivo_privada} y {archivo_publica}")

def firmar_archivo_ecdsa(archivo_privada, archivo_mensaje, archivo_firma, nombre_hash="sha256"):
    priv_der = leer_base64(archivo_privada)
    llave_privada = serialization.load_der_private_key(priv_der, password=None)
    mensaje = Path(archivo_mensaje).read_bytes()
    
    firma_der = llave_privada.sign(mensaje, ec.ECDSA(obtener_hash(nombre_hash)))
    r, s = utils.decode_dss_signature(firma_der) # Extrae el par (r,s) 
    
    with open(archivo_firma, 'w') as f:
        json.dump({"hash": nombre_hash, "r": str(r), "s": str(s)}, f)
    print(f"\n✅ Firma (r, s) guardada en {archivo_firma}")

# =========================================================
# 3. FUNCIONES DEL INTEGRANTE 2 (ECDH + KDF)
# =========================================================

def simular_ecdh(nombre_curva):
    curva = obtener_curva(nombre_curva)
    priv_alice = ec.generate_private_key(curva)
    priv_bob = ec.generate_private_key(curva)
    
    # Intercambio (K = abG) 
    secreto_alice = priv_alice.exchange(ec.ECDH(), priv_bob.public_key())
    
    # Derivación de llave de 256 bits (KDF) 
    k_final = HKDF(hashes.SHA256(), 32, None, b'ecdh_key_derivation').derive(secreto_alice)
    
    print(f"\n✅ Secreto compartido K (Base64): {base64.b64encode(secreto_alice).decode()}")
    print(f"\n✅ Llave derivada k de 256 bits: {base64.b64encode(k_final).decode()}")

# =========================================================
# 4. TU PARTE: INTEGRANTE 3 (Verificación e Interfaz)
# =========================================================

def verificar_firma_ecdsa(archivo_publica, archivo_mensaje, archivo_firma):
    """Diseña e implementa la verificación de firma."""
    try:
        pub_der = leer_base64(archivo_publica)
        llave_publica = serialization.load_der_public_key(pub_der)
        mensaje = Path(archivo_mensaje).read_bytes()
        
        with open(archivo_firma, 'r') as f:
            sig_data = json.load(f)
        
        # Reconstruye la firma desde (r, s) 
        firma_der = utils.encode_dss_signature(int(sig_data["r"]), int(sig_data["s"]))
        
        llave_publica.verify(firma_der, mensaje, ec.ECDSA(obtener_hash(sig_data["hash"])))
        print("\n✅ RESULTADO: La firma es VÁLIDA (True)")
        return True
    except Exception as e:
        print(f"\n❌ RESULTADO: La firma es INVÁLIDA (False). Error: {e}")
        return False

# =========================================================
# INTERFAZ VISUAL MEJORADA (Integrante 3)
# =========================================================

def imprimir_encabezado(titulo):
    print("\n" + "="*50)
    print(f"{titulo.center(50)}")
    print("="*50)

def verificar_firma_ecdsa(archivo_publica, archivo_mensaje, archivo_firma):
    imprimir_encabezado("VERIFICACIÓN DE FIRMA ECDSA")
    try:
        pub_der = leer_base64(archivo_publica)
        llave_publica = serialization.load_der_public_key(pub_der)
        mensaje = Path(archivo_mensaje).read_bytes()
        
        with open(archivo_firma, 'r') as f:
            sig_data = json.load(f)
        
        firma_der = utils.encode_dss_signature(int(sig_data["r"]), int(sig_data["s"]))
        
        # Proceso de verificación [cite: 34, 35]
        llave_publica.verify(firma_der, mensaje, ec.ECDSA(obtener_hash(sig_data["hash"])))
        
        print("\n[ RESULTADO DE VALIDACIÓN ]")
        print("Status: VALIDADO ✅")
        print(f"Mensaje: {archivo_mensaje} es auténtico.")
        print("Retorno: True")
        return True
    except Exception as e:
        print("\n[ RESULTADO DE VALIDACIÓN ]")
        print("Status: ERROR / INVÁLIDO ❌")
        print(f"Detalle: {e}")
        print("Retorno: False")
        return False

def menu():
    while True:
        imprimir_encabezado("Lab 06. ECDSA and ECDH (real world )")
        print(" [1] Generar Par de Llaves (ECDSA)")
        print(" [2] Firmar un Archivo (ECDSA)")
        print(" [3] Verificar Firma Digital (ECDSA)")
        print(" [4] Intercambio de Llaves y KDF (ECDH)")
        print(" [5] Salir del Sistema")
        print("="*50)
        
        op = input("Seleccione una opción > ")
        
        if op == "1":
            print("\n--- CONFIGURACIÓN DE LLAVES ---")
            curva = input("Elija Curva NIST (P-224, P-256, P-384, P-521): ")
            generar_llaves_ecdsa(curva, "private_key.txt", "public_key.txt")
        
        elif op == "2":
            print("\n--- PROCESO DE FIRMA ---")
            archivo = input("Nombre del archivo a firmar (ej. mensaje.txt): ")
            firmar_archivo_ecdsa("private_key.txt", archivo, "firma.txt")
        
        elif op == "3":
            # Esta es tu función principal como Integrante 3 [cite: 30, 31]
            verificar_firma_ecdsa("public_key.txt", "mensaje.txt", "firma.txt")
            input("\nPresione Enter para continuar...")
        
        elif op == "4":
            imprimir_encabezado("SIMULACIÓN ECDH + KDF")
            curva = input("Elija Curva NIST para el acuerdo: ")
            simular_ecdh(curva)
            input("\nPresione Enter para continuar...")
            
        elif op == "5":
            break
if __name__ == "__main__":
    menu()