import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { StatisticsService, StatisticsTimeFrame } from 'src/app/services/statistic.service';
import { GlobalConstants } from 'src/app/shared/global-constants';
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ChartData
} from 'chart.js';

Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend
);
@Component({
  selector: 'app-statistics',
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.scss']
})
export class StatisticsComponent implements OnInit {
  filterForm: FormGroup;
  selectedTab = 0;
  charts: { [key: string]: Chart } = {};
  responseMessage: string = '';

  timeFrameOptions = [
    { value: 'daily', label: 'Daily' },
    { value: 'weekly', label: 'Weekly' },
    { value: 'monthly', label: 'Monthly' },
    { value: 'yearly', label: 'Yearly' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private statisticsService: StatisticsService,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService
  ) {
    this.filterForm = this.formBuilder.group({
      timeFrame: ['monthly'],
      startDate: [null],
      endDate: [null],
      useCustomDates: [false]
    });
  }

  ngOnInit(): void {
    this.loadStatistics();
  }

  onTabChanged(event: MatTabChangeEvent): void {
    this.selectedTab = event.index;
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.ngxService.start();
    
    const params = this.getFilterParams();
    
    switch (this.selectedTab) {
      case 0:
        this.loadRevenueStatistics(params);
        break;
      case 1:
        this.loadProfitStatistics(params);
        break;
      case 2:
        this.loadOrderStatistics(params);
        break;
      case 3:
        this.loadCategoryPerformance();
        break;
      case 4:
        this.loadInventoryTurnover();
        break;
    }
  }

  private getFilterParams(): StatisticsTimeFrame {
    const useCustomDates = this.filterForm.get('useCustomDates')?.value;
    
    if (useCustomDates) {
      return {
        startDate: this.filterForm.get('startDate')?.value,
        endDate: this.filterForm.get('endDate')?.value
      };
    }
    
    return {
      timeFrame: this.filterForm.get('timeFrame')?.value
    };
  }

  private loadRevenueStatistics(params: StatisticsTimeFrame): void {
    this.statisticsService.getRevenueStatistics(params).subscribe({
      next: (response) => {
        this.ngxService.stop();
        this.updateRevenueChart(response);
      },
      error: this.handleError.bind(this)
    });
  }

  private loadProfitStatistics(params: StatisticsTimeFrame): void {
    this.statisticsService.getProfitStatistics(params).subscribe({
      next: (response) => {
        this.ngxService.stop();
        this.updateProfitChart(response);
      },
      error: this.handleError.bind(this)
    });
  }

  private loadOrderStatistics(params: StatisticsTimeFrame): void {
    this.statisticsService.getOrderStatistics(params).subscribe({
      next: (response) => {
        this.ngxService.stop();
        this.updateOrdersChart(response);
      },
      error: this.handleError.bind(this)
    });
  }

  private loadCategoryPerformance(): void {
    const params = this.getFilterParams();
    this.statisticsService.getCategoryPerformance(params.startDate, params.endDate).subscribe({
      next: (response) => {
        this.ngxService.stop();
        this.updateCategoryChart(response);
      },
      error: this.handleError.bind(this)
    });
  }

  private loadInventoryTurnover(): void {
    const params = this.getFilterParams();
    this.statisticsService.getInventoryTurnover(params.startDate, params.endDate).subscribe({
      next: (response) => {
        this.ngxService.stop();
        this.updateInventoryChart(response);
      },
      error: this.handleError.bind(this)
    });
  }

  private updateRevenueChart(data: any): void {
    const chartData = {
      labels: Object.keys(data.revenueByPeriod),
      datasets: [{
        label: 'Revenue',
        data: Object.values(data.revenueByPeriod) as number[], // Ensure data is number[]
        borderColor: 'rgb(75, 192, 192)',
        tension: 0.1
      }]
    };

    this.createOrUpdateChart('revenue', chartData, 'Revenue Over Time');
  }

  private updateProfitChart(data: any): void {
    const chartData = {
      labels: Object.keys(data.profitByPeriod),
      datasets: [{
        label: 'Profit',
        data: Object.values(data.profitByPeriod) as number[], // Ensure data is number[]
        borderColor: 'rgb(54, 162, 235)',
        tension: 0.1
      }]
    };

    this.createOrUpdateChart('profit', chartData, 'Profit Over Time');
  }

  private updateOrdersChart(data: any): void {
    const chartData = {
      labels: Object.keys(data.ordersByPeriod),
      datasets: [{
        label: 'Orders',
        data: Object.values(data.ordersByPeriod) as number[], // Ensure data is number[]
        backgroundColor: 'rgba(153, 102, 255, 0.2)',
        borderColor: 'rgb(153, 102, 255)',
        borderWidth: 1
      }]
    };

    this.createOrUpdateChart('orders', chartData, 'Orders Over Time', 'bar');
  }

  private updateCategoryChart(data: any): void {
    const chartData = {
      labels: data.map((item: any) => item.categoryName),
      datasets: [{
        label: 'Revenue by Category',
        data: data.map((item: any) => item.totalRevenue),
        backgroundColor: [
          'rgba(255, 99, 132, 0.2)',
          'rgba(54, 162, 235, 0.2)',
          'rgba(255, 206, 86, 0.2)',
          'rgba(75, 192, 192, 0.2)',
          'rgba(153, 102, 255, 0.2)'
        ],
        borderColor: [
          'rgb(255, 99, 132)',
          'rgb(54, 162, 235)',
          'rgb(255, 206, 86)',
          'rgb(75, 192, 192)',
          'rgb(153, 102, 255)'
        ],
        borderWidth: 1
      }]
    };

    this.createOrUpdateChart('category', chartData, 'Revenue by Category', 'bar');
  }

  private updateInventoryChart(data: any): void {
    const chartData = {
      labels: data.map((item: any) => item.productName),
      datasets: [{
        label: 'Turnover Rate',
        data: data.map((item: any) => item.turnoverRate),
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgb(75, 192, 192)',
        borderWidth: 1
      }]
    };

    this.createOrUpdateChart('inventory', chartData, 'Inventory Turnover Rate', 'bar');
  }

  private createOrUpdateChart(
    id: string,
    data: ChartData,
    title: string,
    type: 'line' | 'bar' = 'line'
  ): void {
    if (this.charts[id]) {
      this.charts[id].destroy();
    }

    const ctx = document.getElementById(id) as HTMLCanvasElement;
    this.charts[id] = new Chart(ctx, {
      type,
      data,
      options: {
        responsive: true,
        plugins: {
          title: {
            display: true,
            text: title
          }
        }
      }
    });
  }

  private handleError(error: any): void {
    this.ngxService.stop();
    if (error.error?.message) {
      this.responseMessage = error.error?.message;
    } else {
      this.responseMessage = GlobalConstants.genericError;
    }
    this.snackbarService.openSnackBar(this.responseMessage, GlobalConstants.error);
  }
}