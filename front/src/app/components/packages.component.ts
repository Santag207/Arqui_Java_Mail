import { Component, OnInit } from '@angular/core';
import { ComprasService } from '../services/compras.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-packages',
  templateUrl: './packages.component.html',
  styleUrls: ['./packages.component.css']
})
export class PackagesComponent implements OnInit{
  packages: any[] = [];
  constructor(private svc: ComprasService, private router: Router){}
  ngOnInit(){ this.svc.getPackages().subscribe({next: (data:any)=> this.packages = data, error: ()=> this.packages = []}); }
  buy(p:any){ this.router.navigate(['/purchase'], { state: { paquete: p } }); }
}
