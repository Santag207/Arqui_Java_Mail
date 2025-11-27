import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { ComprasService } from '../services/compras.service';

@Component({
  selector: 'app-purchase',
  templateUrl: './purchase.component.html',
  styleUrls: ['./purchase.component.css']
})
export class PurchaseComponent{
  form = this.fb.group({ nombreCliente: ['', Validators.required], telefonoCliente: ['', Validators.required], codigosPaquetes: ['[]', Validators.required], total: [0, Validators.required], idSolicitud: ['S-REQ'] });
  result: any;
  constructor(private fb: FormBuilder, private svc: ComprasService, private router: Router){
    const st = (this.router.getCurrentNavigation() || {}).extras?.state as any;
    if(st?.paquete){ this.form.patchValue({ codigosPaquetes: JSON.stringify([st.paquete.codigo]), total: st.paquete.precio || st.paquete.price }); }
  }
  submit(){
    let payload: any = { ...this.form.value };
    try{ payload.codigosPaquetes = JSON.parse(payload.codigosPaquetes); }catch(e){ payload.codigosPaquetes = [payload.codigosPaquetes]; }
    this.svc.createPurchase(payload).subscribe({ next: (r)=> this.result = r, error: (e)=> this.result = { error: e.message || e } });
  }
}
