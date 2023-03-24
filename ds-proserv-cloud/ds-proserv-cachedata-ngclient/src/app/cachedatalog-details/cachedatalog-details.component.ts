import { CacheDataLog } from '../cachedatalog';
import { Component, OnInit, Input } from '@angular/core';
import { CacheDataLogService } from '../cachedatalog.service';
import { CacheDataLogListComponent } from '../cachedatalog-list/cachedatalog-list.component';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-cachedatalog-details',
  templateUrl: './cachedatalog-details.component.html',
  styleUrls: ['./cachedatalog-details.component.css']
})
export class CacheDataLogDetailsComponent implements OnInit {

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
  
    list(){
      this.router.navigate(['cachedatalogs']);
    }

}
