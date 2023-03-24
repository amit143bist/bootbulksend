import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateCachedatalogComponent } from './update-cachedatalog.component';

describe('UpdateCachedatalogComponent', () => {
  let component: UpdateCachedatalogComponent;
  let fixture: ComponentFixture<UpdateCachedatalogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UpdateCachedatalogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UpdateCachedatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
