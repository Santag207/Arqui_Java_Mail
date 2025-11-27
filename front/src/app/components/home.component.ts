import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent{
  userEmail: string | null = null;
  constructor(private router: Router, private auth: AuthService){
    const u = this.auth.getUser();
    this.userEmail = u?.email || null;
  }
  go(page: string){
    if(page === 'packages') this.router.navigate(['/packages']);
    if(page === 'payment') this.router.navigate(['/payment']);
    if(page === 'profile') this.router.navigate(['/profile']);
  }
}
