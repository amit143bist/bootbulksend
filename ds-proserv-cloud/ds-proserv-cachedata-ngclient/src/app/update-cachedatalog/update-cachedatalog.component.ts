import { CacheDataLog } from '../cachedatalog';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CacheDataLogService } from '../cachedatalog.service';

@Component({
  selector: 'app-update-cachedatalog',
  templateUrl: './update-cachedatalog.component.html',
  styleUrls: ['./update-cachedatalog.component.css']
})
export class UpdateCacheDataLogComponent implements OnInit {

  cacheId: string;
  cacheDataLog: CacheDataLog;

  constructor(private route: ActivatedRoute,private router: Router,
    private cacheDataLogService: CacheDataLogService) { }

  ngOnInit() {
    this.cacheDataLog = new CacheDataLog();
  
    this.cacheId = this.route.snapshot.params['cacheId'];
      
    this.cacheDataLogService.getCacheDataLog(this.cacheId)
      .subscribe(data => {
        console.log(data)
        this.cacheDataLog = data;
      }, error => console.log(error));
  }

  updateCacheDataLog() {
    this.cacheDataLogService.updateCacheDataLog(this.cacheId, this.cacheDataLog)
      .subscribe(data => {
        console.log(data);
        this.cacheDataLog = new CacheDataLog();
        this.gotoList();
      }, error => console.log(error));
  }

  onSubmit() {
    this.updateCacheDataLog();    
  }

  gotoList() {
    this.router.navigate(['/cachedatalogs']);
  }

}
