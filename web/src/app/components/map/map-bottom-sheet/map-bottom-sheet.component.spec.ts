import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { MapBottomSheetComponent } from './map-bottom-sheet.component';

describe('MapBottomSheetComponent', () => {
  let component: MapBottomSheetComponent;
  let fixture: ComponentFixture<MapBottomSheetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ MapBottomSheetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapBottomSheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
