import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrestamoForm } from './prestamo-form';

describe('PrestamoForm', () => {
  let component: PrestamoForm;
  let fixture: ComponentFixture<PrestamoForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PrestamoForm],
    }).compileComponents();

    fixture = TestBed.createComponent(PrestamoForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
