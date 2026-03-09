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
}Punto1, Punto2;

void ValidarPunto(Punto &p);
string transformarABinario(int n);
void LTR(string &k_binario);
void RTL(string &k_binario);
// Prototipos de las funciones que implementaremos después
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
    RTL(k_bin);
    LTR(k_bin);
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

void LTR(string &k_binario){
    reverse(k_binario.begin(), k_binario.end());
    cout << "Valor de k en binario (Little-Endian): " << k_binario << endl;
}

void RTL(string &k_binario){
    cout << "Valor de k en binario (Big-Endian): " << k_binario << endl;
}

Punto sumarPuntos(Punto A, Punto B) {
    return A; 
}

Punto doblarPunto(Punto A) {
    return A;
}