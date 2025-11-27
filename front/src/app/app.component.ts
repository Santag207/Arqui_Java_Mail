import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  template: `
    <div class="navbar">
      <a style="color:white; text-decoration:none; margin-right:16px;" routerLink="/home">TVP</a>
      <a style="color:white; text-decoration:none; margin-right:8px;" routerLink="/packages">Paquetes</a>
      <a *ngIf="!auth.isAuthenticated()" style="color:white; text-decoration:none; margin-right:8px;" routerLink="/login">Login</a>
      <a *ngIf="auth.isAuthenticated()" style="color:white; text-decoration:none; margin-right:8px;" routerLink="/profile">Perfil</a>
      <a *ngIf="auth.isAuthenticated()" style="color:white; text-decoration:none; margin-right:8px;" routerLink="/payment">Pago</a>
      <button *ngIf="auth.isAuthenticated()" (click)="logout()" style="float:right;">Cerrar sesión</button>
    </div>
    <div class="container">
      <router-outlet></router-outlet>
    </div>
  `
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}
  logout(){ this.auth.logout(); this.router.navigate(['/']); }
}
