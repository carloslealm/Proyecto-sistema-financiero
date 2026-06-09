import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
 
@Component({
  selector: 'app-recuperar',
  templateUrl: './recuperar.html',
  styleUrls: ['./recuperar.scss'],
  standalone: false
})
export class RecuperarComponent implements OnInit {
 
  form!: FormGroup;
  loading  = false;
  enviado  = false;
  error    = '';
 
  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}
 
  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }
 
  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
 
    this.loading = true;
    this.error   = '';
 
    this.http.post<any>(
      'http://localhost:8080/api/v1/auth/recuperar-password',
      { email: this.form.value.email }
    ).subscribe({
      next: () => {
        this.loading = false;
        this.enviado = true;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.error   = 'Error al procesar la solicitud. Intenta de nuevo.';
        this.cdr.detectChanges();
      }
    });
  }
 
  irALogin(): void {
    this.router.navigate(['/auth/login']);
  }
 
  get email() { return this.form.get('email')!; }
}