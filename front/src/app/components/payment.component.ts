import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit{
  cards: any[] = [];
  balance = 0;
  constructor(private auth: AuthService, private http: HttpClient){}
  ngOnInit(){
    // Demo: there is no payment-service running here. We show demo data.
    this.cards = [ { last4: '4242', type: 'VISA', balance: 150.00 }, { last4: '1111', type: 'MC', balance: 75.5 } ];
    this.balance = this.cards.reduce((s,c)=> s + (c.balance||0), 0);
  }
}
