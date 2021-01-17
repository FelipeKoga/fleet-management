import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PttSnackbarComponent } from './ptt-snackbar.component';

describe('PttSnackbarComponent', () => {
  let component: PttSnackbarComponent;
  let fixture: ComponentFixture<PttSnackbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PttSnackbarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PttSnackbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
