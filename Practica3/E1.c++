#include <iostream>
#include <string>
#include <algorithm>

using namespace std;

int k;
int P;
string k_bin;

struct Punto{
    long long x;
    long long y;
    long long z;
}Punto1;

void ValidarPunto(Punto &p);
string transformarABinario(int n);
//Punto LTR(Punto P_ingresado, string k_binario);
Punto RTL(Punto P_ingresado, string k_binario);

Punto sumarPuntos(Punto A, Punto B);
Punto doblarPunto(Punto A);

int main(){
    cout<<"Ingrese las coordenadas del punto (x,y) separadas por espacios: "<<endl;
    cin>>Punto1.x>>Punto1.y;

    ValidarPunto(Punto1);
    
    cout<<"Ingrese el valor de k "; cin>>k;
    k_bin = transformarABinario(k);
    cout<<"Ingrese el valor de P "; cin>>P;

    cout << "-----------------------------------" << endl;
    cout<<"El punto ingresado es: ("<<Punto1.x<<","<<Punto1.y<<","<<Punto1.z<<")"<<endl;
    cout<<"El valor de k es: "<<k<< " el valor de P es: "<<P<<endl; 
    //Punto resultado = LTR(Punto1, k_bin);
    Punto resultado = RTL(Punto1, k_bin);
    
    return 0;
}
void ValidarPunto(Punto &p) {
    if (p.x == 0 && p.y == 1) {
        p.z = 0; 
        cout << "ERROR - Punto al infinito detectado O (0,1,0)"<< endl;
        exit(1);
    } else {
        p.z = 1;
    }
}

string transformarABinario(int n) {
    if (n == 0) return "0";
    
    string binario = "";
    while (n > 0) {
        if (n % 2 == 0) {
            binario += "0";
        } else {
            binario += "1";
        }
        n = n / 2; // División entera
    }
    return binario;
}

Punto RTL(Punto P_ingresado, string k_binario) {
    // Paso 1: Q = infinito (usando tu definición de z=0)
    Punto Q = {0, 1, 0}; 
    // Paso 2: P = P (el punto que se irá doblando)
    Punto P_aux = P_ingresado;
    // El ciclo recorre desde i = 0 hasta t-1 (longitud de la cadena)
    for (int i = 0; i < k_binario.length(); i++) {
        // Paso 3 y 4: if k_i == 1 then Q = Q + P
        if (k_binario[i] == '1') {
            cout << "[RTL] Bit 1 en posicion " << i << ": Sumando..." << endl;
            Q = sumarPuntos(Q, P_aux);
        }
        // Paso 6: P = 2P (Doblado constante en cada iteración)
        P_aux = doblarPunto(P_aux);
    } 
    // Paso 8: return Q
    return Q;
}

Punto LTR(Punto P_ingresado, string k_binario) {
    // IMPORTANTE: Invertimos para que la posicion 0 sea el bit t-1 (MSB)
    reverse(k_binario.begin(), k_binario.end());

    // 1. Q = infinito
    Punto Q = {0, 1, 0};

    // 2. for i from t-1 down to 0
    for (int i = 0; i < k_binario.length(); i++) {
        // 3. Q = 2Q (Point Doubling)
        Q = doblarPunto(Q);

        // 4. if k_i == 1
        if (k_binario[i] == '1') {
            // 5. Q = Q + P (Point Addition)
            Q = sumarPuntos(Q, P_ingresado);
        }
    }
    // 8. return Q
    return Q;
}

Punto sumarPuntos(Punto A, Punto B) {
    return A; 
}

Punto doblarPunto(Punto A) {
    return A;
}