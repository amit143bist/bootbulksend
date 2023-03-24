import { CacheDataLogService } from '../cachedatalog.service';
import { CacheDataLog } from '../cachedatalog';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-create-cachedatalog',
  templateUrl: './create-cachedatalog.component.html',
  styleUrls: ['./create-cachedatalog.component.css']
})
export class CreateCacheDataLogComponent implements OnInit {

  cacheDataLog: CacheDataLog = new CacheDataLog();
  submitted = false;

  constructor(private cacheDataLogService: CacheDataLogService,
    private router: Router) { }

  ngOnInit() {
  }

  newCacheDataLog(): void {
    this.submitted = false;
    this.cacheDataLog = new CacheDataLog();
  }

  save() {
    this.cacheDataLogService
    .createCacheDataLog(this.cacheDataLog).subscribe(data => {
      console.log(data)
      this.cacheDataLog = new CacheDataLog();
      this.gotoList();
    }, 
    error => console.log(error));
  }

  onSubmit() {
    this.submitted = true;
    this.save();    
  }

  gotoList() {
    this.router.navigate(['/cachedatalogs']);
  }

}
