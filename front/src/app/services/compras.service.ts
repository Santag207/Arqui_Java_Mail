import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ComprasService {
  constructor(private http: HttpClient) {}
  // Packages list - adjust endpoint as needed
  getPackages(){ return this.http.get<any[]>(`${environment.comprasServiceUrl}/api/paquetes`); }
  // List purchases
  getPurchases(){ return this.http.get<any[]>(`${environment.comprasServiceUrl}/api/compras`); }
  // Create purchase
  createPurchase(payload: any){ return this.http.post(`${environment.comprasServiceUrl}/api/compras/procesar`, payload); }
}
