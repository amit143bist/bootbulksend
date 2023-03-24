import { TestBed } from '@angular/core/testing';

import { CachedatalogService } from './cachedatalog.service';

describe('CachedatalogService', () => {
  let service: CachedatalogService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CachedatalogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
