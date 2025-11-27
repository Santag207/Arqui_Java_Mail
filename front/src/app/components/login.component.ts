import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  form = this.fb.group({ email: ['', [Validators.required]], password: ['', [Validators.required]] });
  error = '';
  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router){}
  submit(){
    const v = this.form.value;
    this.auth.login(v.email, v.password).subscribe({
      next: (res:any) => {
        if(res && res.token){
          this.auth.storeToken(res.token);
          // store user info if provided
          this.auth.storeUser({ email: res.email, rol: res.rol });
          this.router.navigate(['/home']);
        }
      },
      error: (err) => { this.error = 'Error de autenticación'; }
    });
  }
}
