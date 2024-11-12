import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { ProductStatistics, StatisticsService } from 'src/app/services/statistic.service';

// Register Chart.js components
Chart.register(...registerables);

@Component({
  selector: 'app-statistics-dashboard',
  templateUrl: './statistics-dashboard.component.html',
  styleUrls: ['./statistics-dashboard.component.scss']
})
export class StatisticsDashboardComponent implements OnInit {
  dashboardSummary: any;
  filterForm: FormGroup;
  revenueChart: Chart | undefined;
  orderChart: Chart | undefined;
  categoryChart: Chart | undefined;
  profitChart: Chart | undefined;
  inventoryChart: Chart | undefined;
  bestSellingChart: Chart | undefined;

  constructor(
    private statisticsService: StatisticsService,
    private formBuilder: FormBuilder,
    private datePipe: DatePipe
  ) {
    this.filterForm = this.formBuilder.group({
      timeFrame: ['monthly'],
      specificDate: [''],
      startDate: [''],
      endDate: ['']
    });
  }

  ngOnInit() {
    // Load initial dashboard data
    this.loadDashboardSummary();
  }

  loadDashboardSummary() {
    this.statisticsService.getDashboardSummary().subscribe(data => {
      this.dashboardSummary = data;
      // Load all statistics including best sellers right after getting dashboard summary
      this.loadAllStatistics();
      // Setup best selling chart immediately with the data from dashboard summary
      if (this.dashboardSummary?.bestSellers) {
        this.setupBestSellingChart(this.dashboardSummary.bestSellers);
      }
    });
  }

  loadAllStatistics() {
    const filters = this.filterForm.value;
    this.loadRevenueStatistics(filters);
    this.loadOrderStatistics(filters);
    this.loadCategoryPerformance(filters);
    this.loadProfitStatistics(filters);
    this.loadInventoryTurnover(filters);
  }

  setupBestSellingChart(data: ProductStatistics[]) {
    const ctx = document.getElementById('bestSellingChart') as HTMLCanvasElement;
    if (this.bestSellingChart) {
      this.bestSellingChart.destroy();
    }

    // Sort products by quantity sold in descending order
    const sortedData = [...data].sort((a, b) => b.totalQuantitySold - a.totalQuantitySold);
    // Take top 5 best selling products
    const topProducts = sortedData.slice(0, 5);

    const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels: data.map(item => item.productName),
        datasets: [
          {
            label: 'Quantity Sold',
            data: data.map(item => item.totalQuantitySold),
            backgroundColor: 'rgba(75, 192, 192, 0.5)',
            yAxisID: 'y'
          },
          {
            label: 'Average Rating',
            data: topProducts.map(item => item.averageRating),
            backgroundColor: 'rgba(255, 159, 64, 0.5)',
            yAxisID: 'y1',
            type: 'line', // Changed to line type for better visualization
            borderColor: 'rgba(255, 159, 64, 1)',
            pointBackgroundColor: 'rgba(255, 159, 64, 1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(255, 159, 64, 1)'
          }
        ]
      },
      options: {
        responsive: true,
        interaction: {
          mode: 'index',
          intersect: false,
        },
        plugins: {
          title: {
            display: true,
            text: 'Top 5 Best Selling Products'
          },
          tooltip: {
            enabled: true,
            mode: 'index'
          },
          legend: {
            position: 'top',
          }
        },
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: 'Products'
            },
            ticks: {
              maxRotation: 45,
              minRotation: 45
            }
          },
          y: {
            display: true,
            position: 'left',
            title: {
              display: true,
              text: 'Quantity Sold'
            },
            beginAtZero: true
          },
          y1: {
            display: true,
            position: 'right',
            title: {
              display: true,
              text: 'Average Rating'
            },
            min: 0,
            max: 5,
            grid: {
              drawOnChartArea: false // only show grid lines for primary y-axis
            }
          }
        }
      }
    };

    this.bestSellingChart = new Chart(ctx, config);
  }

  loadRevenueStatistics(filters: any) {
    this.statisticsService.getRevenueStatistics(
      filters.timeFrame,
      filters.specificDate,
      filters.startDate,
      filters.endDate
    ).subscribe(data => {
      this.setupRevenueChart(data);
    });
  }

  setupRevenueChart(data: any) {
    const sortedData = this.sortDataChronologically(data.revenueByPeriod);

    const ctx = document.getElementById('revenueChart') as HTMLCanvasElement;
    if (this.revenueChart) {
      this.revenueChart.destroy();
    }

    const config: ChartConfiguration = {
      type: 'line',
      data: {
        labels: Object.keys(sortedData),
        datasets: [{
          label: 'Revenue',
          data: Object.values(sortedData) as number[],
          borderColor: 'rgb(75, 192, 192)',
          tension: 0.1
        }]
      },
      options: {
        responsive: true,
        scales: {
          x: {
            display: true
          },
          y: {
            display: true,
            beginAtZero: true
          }
        }
      }
    };
    
    this.revenueChart = new Chart(ctx, config);
  }

  loadOrderStatistics(filters: any) {
    this.statisticsService.getOrderStatistics(
      filters.timeFrame,
      filters.specificDate,
      filters.startDate,
      filters.endDate
    ).subscribe(data => {
      this.setupOrderChart(data);
    });
  }

  setupOrderChart(data: any) {
    const sortedData = this.sortDataChronologically(data.ordersByPeriod);

    const ctx = document.getElementById('orderChart') as HTMLCanvasElement;
    if (this.orderChart) {
      this.orderChart.destroy();
    }
    
     const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels: Object.keys(sortedData),
        datasets: [{
          label: 'Orders',
          data: Object.values(sortedData) as number[],
          backgroundColor: 'rgba(54, 162, 235, 0.5)'
        }]
      },
      options: {
        responsive: true,
        scales: {
          x: {
            display: true
          },
          y: {
            display: true,
            beginAtZero: true
          }
        }
      }
    };

    this.orderChart = new Chart(ctx, config);
  }

  private sortDataChronologically(data: { [key: string]: number }): { [key: string]: number } {
    return Object.fromEntries(
      Object.entries(data).sort(([dateA], [dateB]) => {
        return new Date(dateA).getTime() - new Date(dateB).getTime();
      })
    );
  }
  
  loadCategoryPerformance(filters: any) {
    this.statisticsService.getCategoryPerformance(
      filters.timeFrame,
      filters.specificDate,
      filters.startDate,
      filters.endDate
    ).subscribe(data => {
      this.setupCategoryChart(data);
    });
  }

  setupCategoryChart(data: any) {
    const ctx = document.getElementById('categoryChart') as HTMLCanvasElement;
    if (this.categoryChart) {
      this.categoryChart.destroy();
    }
    
    const config: ChartConfiguration = {
      type: 'pie',
      data: {
        labels: data.map((item: any) => item.categoryName),
        datasets: [{
          data: data.map((item: any) => item.totalRevenue) as number[],
          backgroundColor: [
            'rgba(255, 99, 132, 0.5)',
            'rgba(54, 162, 235, 0.5)',
            'rgba(255, 206, 86, 0.5)',
            'rgba(75, 192, 192, 0.5)',
            'rgba(153, 102, 255, 0.5)'
          ]
        }]
      },
      options: {
        responsive: true
      }
    };

    this.categoryChart = new Chart(ctx, config);
  }

  loadProfitStatistics(filters: any) {
    this.statisticsService.getProfitStatistics(
      filters.timeFrame,
      filters.specificDate,
      filters.startDate,
      filters.endDate
    ).subscribe(data => {
      this.setupProfitChart(data);
    });
  }

  setupProfitChart(data: any) {
    const sortedData = this.sortDataChronologically(data.profitByPeriod);

    const ctx = document.getElementById('profitChart') as HTMLCanvasElement;
    if (this.profitChart) {
      this.profitChart.destroy();
    }
    
    const config: ChartConfiguration = {
      type: 'line',
      data: {
        labels: Object.keys(sortedData),
        datasets: [{
          label: 'Profit',
          data: Object.values(sortedData) as number[],
          borderColor: 'rgb(255, 99, 132)',
          tension: 0.1
        }]
      },
      options: {
        responsive: true,
        scales: {
          x: {
            display: true
          },
          y: {
            display: true,
            beginAtZero: true
          }
        }
      }
    };

    this.profitChart = new Chart(ctx, config);
  }

  loadInventoryTurnover(filters: any) {
    this.statisticsService.getInventoryTurnover(
      filters.timeFrame,
      filters.specificDate,
      filters.startDate,
      filters.endDate
    ).subscribe(data => {
      this.setupInventoryChart(data);
    });
  }

  setupInventoryChart(data: any) {
    const ctx = document.getElementById('inventoryChart') as HTMLCanvasElement;
    if (this.inventoryChart) {
      this.inventoryChart.destroy();
    }
      
    const config: ChartConfiguration = {
      type: 'bar',
      data: {
        labels: data.map((item: any) => item.productName),
        datasets: [
          {
            label: 'Beginning Inventory',
            data: data.map((item: any) => item.beginningInventory),
            backgroundColor: 'rgba(54, 162, 235, 0.5)',
            order: 1
          },
          {
            label: 'Ending Inventory',
            data: data.map((item: any) => item.endingInventory),
            backgroundColor: 'rgba(75, 192, 192, 0.5)',
            order: 2
          },
          {
            label: 'Sold Quantity',
            data: data.map((item: any) => item.soldQuantity),
            backgroundColor: 'rgba(255, 206, 86, 0.5)',
            order: 3
          },
          {
            label: 'Turnover Rate',
            data: data.map((item: any) => item.turnoverRate),
            type: 'line',
            borderColor: 'rgba(255, 99, 132, 1)',
            borderWidth: 2,
            fill: false,
            yAxisID: 'y1',
            order: 0
          }
        ]
      },
      options: {
        responsive: true,
        interaction: {
          intersect: false,
          mode: 'index'
        },
        plugins: {
          title: {
            display: true,
            text: 'Inventory Turnover Analysis'
          },
          tooltip: {
            enabled: true,
            callbacks: {
              label: function(context) {
                let label = context.dataset.label || '';
                if (label) {
                  label += ': ';
                }
                if (context.parsed.y !== null) {
                  if (label.includes('Rate')) {
                    label += context.parsed.y.toFixed(2);
                  } else {
                    label += context.parsed.y.toFixed(0);
                  }
                }
                return label;
              }
            }
          }
        },
        scales: {
          x: {
            ticks: {
              maxRotation: 45,
              minRotation: 45
            }
          },
          y: {
            type: 'linear',
            display: true,
            position: 'left',
            title: {
              display: true,
              text: 'Quantity'
            },
            grid: {
              drawOnChartArea: true
            }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            title: {
              display: true,
              text: 'Turnover Rate'
            },
            min: 0,
            max: Math.max(...data.map((item: any) => item.turnoverRate)) * 1.2,
            grid: {
              drawOnChartArea: false
            }
          }
        }
      }
    };
  
    this.inventoryChart = new Chart(ctx, config);
  }

  onFilterChange() {
    this.loadAllStatistics();
    // Refresh best selling chart with current dashboard data
    if (this.dashboardSummary?.bestSellers) {
      this.setupBestSellingChart(this.dashboardSummary.bestSellers);
    }
  }
}