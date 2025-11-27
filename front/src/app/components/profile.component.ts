import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit{
  user: any = null;
  constructor(private auth: AuthService, private http: HttpClient){}
  ngOnInit(){
    const token = this.auth.getToken();
    if(token){ this.http.get<any>(`${environment.authServiceUrl}/auth/validate`, { headers: { Authorization: 'Bearer ' + token } }).subscribe({ next: r=> this.user = r, error: ()=> this.user = null }); }
  }
}
