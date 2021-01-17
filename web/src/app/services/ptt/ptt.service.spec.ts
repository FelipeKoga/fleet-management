import { TestBed } from '@angular/core/testing';

import { PttService } from './ptt.service';

describe('PttService', () => {
  let service: PttService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PttService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
