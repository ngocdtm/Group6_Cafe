import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoveStockComponent } from './remove-stock.component';

describe('RemoveStockComponent', () => {
  let component: RemoveStockComponent;
  let fixture: ComponentFixture<RemoveStockComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RemoveStockComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RemoveStockComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
