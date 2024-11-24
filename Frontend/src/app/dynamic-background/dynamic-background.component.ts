import { Component, NgZone, OnInit } from '@angular/core';

@Component({
  selector: 'app-dynamic-background',
  templateUrl: './dynamic-background.component.html',
  styleUrls: ['./dynamic-background.component.scss']
})
export class DynamicBackgroundComponent implements OnInit {

  images = [
    { 
      url: 'assets/img/dynamic_banner_coffee_1.jpg', 
      title: 'Welcome to Our Cafe',
      subtitle: 'Experience the perfect blend of comfort and taste'
    },
    { 
      url: 'assets/img/dynamic_banner_coffee_2.jpg', 
      title: 'Artisan Coffee',
      subtitle: 'Handcrafted with passion and expertise'
    },
    { 
      url: 'assets/img/dynamic_banner_coffee_3.jpg', 
      title: 'Cozy Atmosphere',
      subtitle: 'Your perfect spot for relaxation'
    }
    // { 
    //   url: 'assets/images/cafe4.jpg', 
    //   title: 'Fresh Delights',
    //   subtitle: 'Discover our daily baked goods'
    // }
  ];

  currentImageIndex = 0;
  private interval: any;
  slideDuration = 6000; // 6 seconds
  private imageCache: HTMLImageElement[] = [];

  constructor(private ngZone: NgZone) {}

  ngOnInit() {
    this.preloadImages();
    this.startSlideshow();
  }

  ngOnDestroy() {
    this.stopSlideshow();
    this.imageCache = [];
  }

  private preloadImages() {
    this.images.forEach(image => {
      const img = new Image();
      img.src = image.url;
      this.imageCache.push(img);
    });
  }

  startSlideshow() {
    // Run outside Angular zone for better performance
    this.ngZone.runOutsideAngular(() => {
      this.interval = setInterval(() => {
        this.ngZone.run(() => {
          this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
        });
      }, this.slideDuration);
    });
  }

  stopSlideshow() {
    if (this.interval) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }

  selectImage(index: number) {
    if (index === this.currentImageIndex) return;
    
    this.stopSlideshow();
    this.currentImageIndex = index;
    this.startSlideshow();
  }
}