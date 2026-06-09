import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
 
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false
})
export class LoginComponent implements OnInit {
 
  loginForm!: FormGroup;
  loading  = false;
  errorMsg = '';
  showPass = false;
 
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}
 
  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }
 
    this.loginForm = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }
 
  get email()    { return this.loginForm.get('email')!; }
  get password() { return this.loginForm.get('password')!; }
 
  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
 
    this.loading  = true;
    this.errorMsg = '';
 
    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.loading  = false;
        this.errorMsg = err.error?.message ?? 'Credenciales inválidas';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
      irARecuperar(): void {
      this.router.navigate(['/auth/recuperar']);
      }
      
      irARegistro(): void {
        this.router.navigate(['/auth/registro']);
      }
}