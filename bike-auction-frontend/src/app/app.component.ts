
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthService, AuthUser } from './auth.service';

interface Auction {
  id?: string;
  auctionId?: string;
  title: string;
  startingPrice: number;
  currentPrice?: number;
  minimumIncrement?: number;
  status?: string;
  startsAt?: string;
  endsAt?: string;
  highestBid?: number;
  currentHighestBid?: number;
}

interface Bid {
  id?: string;
  auctionId?: string;
  bidderId?: string;
  amount: number;
  requestId?: string;
  createdAt?: string;
  placedAt?: string;
}


type AuctionFilter =
  | 'ALL'
  | 'LIVE'
  | 'SCHEDULED'
  | 'CLOSED'
  | 'EXPIRED'
  | 'CANCELLED';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly apiUrl = 'https://bike-auction-platform667.onrender.com/api/auctions';

  auctions: Auction[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  searchTerm = '';
  statusFilter: AuctionFilter = 'ALL';

  showCreateForm = false;
  creating = false;

  currentUser: AuthUser | null = null;
  showAuthForm = false;
  authMode: 'login' | 'register' = 'login';
  authSubmitting = false;
  authError = '';
  authSuccess = '';


  bidAmounts: Record<string, number> = {};
  bidHistoryByAuction: Record<string, Bid[]> = {};
  bidHistoryVisible: Record<string, boolean> = {};
  submittingBidIds = new Set<string>();


  loginData = {
    email: '',
    password: ''
  };

  registerData = {
    displayName: '',
    email: '',
    password: ''
  };

  newAuction = {
    title: '',
    startingPrice: 50000,
    minimumIncrement: 500,
    startsAt: '',
    endsAt: ''
  };

  constructor(
    private readonly http: HttpClient,
    public readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.loadAuctions();
  }

  // -------------------------
  // AUCTION LOADING
  // -------------------------

  loadAuctions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<Auction[]>(this.apiUrl).subscribe({
      next: data => {
        this.auctions = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: error => {
        this.loading = false;
        this.errorMessage =
          error.error?.message ||
          'Could not connect to the auction server. Make sure your Spring Boot backend is running on port 8085.';

        console.error('Failed to load auctions:', error);
      }
    });
  }

  // -------------------------
  // EFFECTIVE AUCTION STATUS
  // -------------------------

  /**
   * A scheduled or live auction is treated as expired in the UI
   * once its end time has passed, even if the stored status is stale.
   */
  getEffectiveStatus(auction: Auction): string {
    const status = (auction.status || 'SCHEDULED').toUpperCase();

    if (status === 'CLOSED' || status === 'CANCELLED') {
      return status;
    }

    if (
      (status === 'SCHEDULED' || status === 'LIVE') &&
      auction.endsAt &&
      new Date(auction.endsAt).getTime() <= Date.now()
    ) {
      return 'EXPIRED';
    }

    return status;
  }

  isAuctionExpired(auction: Auction): boolean {
    return this.getEffectiveStatus(auction) === 'EXPIRED';
  }

  canStartAuction(auction: Auction): boolean {
    if (this.getEffectiveStatus(auction) !== 'SCHEDULED') {
      return false;
    }

    if (!auction.startsAt || !auction.endsAt) {
      return false;
    }

    const now = Date.now();
    const startTime = new Date(auction.startsAt).getTime();
    const endTime = new Date(auction.endsAt).getTime();

    return (
      Number.isFinite(startTime) &&
      Number.isFinite(endTime) &&
      startTime <= now &&
      endTime > now
    );
  }

  canCloseAuction(auction: Auction): boolean {
    return this.getEffectiveStatus(auction) === 'LIVE';
  }

  // -------------------------
  // SEARCH AND FILTERS
  // -------------------------

  get filteredAuctions(): Auction[] {
    const query = this.searchTerm.trim().toLowerCase();

    return this.auctions.filter(auction => {
      const matchesSearch = (auction.title || '')
        .toLowerCase()
        .includes(query);

      const effectiveStatus = this.getEffectiveStatus(auction);

      const matchesStatus =
        this.statusFilter === 'ALL' ||
        effectiveStatus === this.statusFilter;

      return matchesSearch && matchesStatus;
    });
  }

  // -------------------------
  // STATISTICS
  // -------------------------

  get liveCount(): number {
    return this.auctions.filter(
      auction => this.getEffectiveStatus(auction) === 'LIVE'
    ).length;
  }

  get scheduledCount(): number {
    return this.auctions.filter(
      auction => this.getEffectiveStatus(auction) === 'SCHEDULED'
    ).length;
  }

  get closedCount(): number {
    return this.auctions.filter(
      auction => this.getEffectiveStatus(auction) === 'CLOSED'
    ).length;
  }

  get expiredCount(): number {
    return this.auctions.filter(
      auction => this.getEffectiveStatus(auction) === 'EXPIRED'
    ).length;
  }

  // -------------------------
  // AUCTION DISPLAY HELPERS
  // -------------------------

  getAuctionId(auction: Auction): string {
    return auction.auctionId ?? auction.id ?? '';
  }

  getDisplayPrice(auction: Auction): number {
    return (
      auction.currentHighestBid ??
      auction.highestBid ??
      auction.currentPrice ??
      auction.startingPrice ??
      0
    );
  }

  getStatusClass(status?: string): string {
    switch ((status ?? 'SCHEDULED').toUpperCase()) {
      case 'LIVE':
        return 'live';

      case 'CLOSED':
        return 'closed';

      case 'EXPIRED':
        return 'expired';

      case 'CANCELLED':
        return 'cancelled';

      case 'SCHEDULED':
      default:
        return 'scheduled';
    }
  }

  trackByAuction = (_index: number, auction: Auction): string => {
    return auction.auctionId ?? auction.id ?? String(_index);
  };

  // -------------------------
  // AUTHENTICATION
  // -------------------------

  openAuthForm(mode: 'login' | 'register' = 'login'): void {
    this.authMode = mode;
    this.showAuthForm = true;
    this.authError = '';
    this.authSuccess = '';
  }

  closeAuthForm(): void {
    this.showAuthForm = false;
    this.authError = '';
    this.authSuccess = '';
  }

  switchAuthMode(mode: 'login' | 'register'): void {
    this.authMode = mode;
    this.authError = '';
    this.authSuccess = '';
  }

  submitAuth(): void {
    this.authError = '';
    this.authSuccess = '';
    this.authSubmitting = true;

    if (this.authMode === 'register') {
      this.authService.register(this.registerData).subscribe({
        next: () => {
          this.authSubmitting = false;
          this.authMode = 'login';

          this.loginData = {
            email: this.registerData.email,
            password: ''
          };

          this.registerData = {
            displayName: '',
            email: '',
            password: ''
          };

          this.authSuccess = 'Account created. Please log in.';
        },
        error: error => {
          this.authSubmitting = false;

          this.authError =
            error.error?.message ||
            (error.status === 409
              ? 'An account with this email already exists.'
              : 'Could not create your account. Please check your details.');

          console.error('Registration failed:', error);
        }
      });

      return;
    }

    this.authService.login(this.loginData).subscribe({
      next: user => {
        this.currentUser = {
          id: user.id,
          displayName: user.displayName,
          email: user.email,
          role: user.role
        };

        this.authSubmitting = false;
        this.showAuthForm = false;
        this.loginData.password = '';

        this.successMessage = `Welcome back, ${user.displayName}!`;
        this.loadAuctions();
      },
      error: error => {
        this.authSubmitting = false;

        this.authError =
          error.error?.message ||
          'Login failed. Check your email and password.';

        console.error('Login failed:', error);
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.currentUser = null;
    this.showCreateForm = false;

    this.successMessage = 'You have logged out.';
    this.errorMessage = '';
  }

  // -------------------------
  // CREATE AUCTION
  // -------------------------

  openCreateForm(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.currentUser || !this.authService.getToken()) {
      this.openAuthForm('login');
      this.authError = 'Please log in before creating an auction.';
      return;
    }

    this.showCreateForm = true;
  }

  closeCreateForm(): void {
    this.showCreateForm = false;
  }

  createAuction(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.currentUser || !this.authService.getToken()) {
      this.openAuthForm('login');
      this.authError = 'Please log in before creating an auction.';
      return;
    }

    const title = this.newAuction.title.trim();

    if (!title) {
      this.errorMessage = 'Please enter an auction title.';
      return;
    }

    if (
      !Number.isFinite(this.newAuction.startingPrice) ||
      this.newAuction.startingPrice < 0.01
    ) {
      this.errorMessage = 'Starting price must be greater than zero.';
      return;
    }

    if (
      !Number.isFinite(this.newAuction.minimumIncrement) ||
      this.newAuction.minimumIncrement < 0.01
    ) {
      this.errorMessage = 'Minimum increment must be greater than zero.';
      return;
    }

    if (!this.newAuction.startsAt || !this.newAuction.endsAt) {
      this.errorMessage = 'Please provide both the start and end times.';
      return;
    }

    const startTime = new Date(this.newAuction.startsAt);
    const endTime = new Date(this.newAuction.endsAt);
    const now = new Date();

    if (
      !Number.isFinite(startTime.getTime()) ||
      !Number.isFinite(endTime.getTime())
    ) {
      this.errorMessage = 'Please enter valid start and end times.';
      return;
    }

    if (startTime.getTime() < now.getTime()) {
      this.errorMessage = 'The auction start time cannot be in the past.';
      return;
    }

    if (endTime.getTime() <= startTime.getTime()) {
      this.errorMessage = 'End time must be after start time.';
      return;
    }

    const payload = {
      title,
      startingPrice: this.newAuction.startingPrice,
      minimumIncrement: this.newAuction.minimumIncrement,
      startsAt: startTime.toISOString(),
      endsAt: endTime.toISOString()
    };

    this.creating = true;

    this.http.post<Auction>(this.apiUrl, payload).subscribe({
      next: () => {
        this.creating = false;
        this.showCreateForm = false;
        this.successMessage = 'Auction created successfully!';

        this.newAuction = {
          title: '',
          startingPrice: 50000,
          minimumIncrement: 500,
          startsAt: '',
          endsAt: ''
        };

        this.loadAuctions();
      },
      error: error => {
        this.creating = false;

        this.errorMessage =
          error.error?.message ||
          'Unable to create the auction. Please check the details and try again.';

        console.error('Failed to create auction:', error);
      }
    });
  }

  // -------------------------
  // START AUCTION
  // -------------------------

  startAuction(auction: Auction): void {
    if (!this.currentUser || !this.authService.getToken()) {
      this.openAuthForm('login');
      this.authError = 'Please log in to manage auctions.';
      return;
    }

    if (!this.canStartAuction(auction)) {
      this.errorMessage =
        this.isAuctionExpired(auction)
          ? 'This auction has expired and cannot be started.'
          : 'This auction is not ready to start. Check its scheduled start time.';

      return;
    }

    const id = this.getAuctionId(auction);

    if (!id) {
      this.errorMessage = 'Auction ID is missing.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<Auction>(`${this.apiUrl}/${id}/start`, {}).subscribe({
      next: () => {
        this.successMessage = 'Auction started successfully.';
        this.loadAuctions();
      },
      error: error => {
        this.errorMessage =
          error.error?.message ||
          (error.status === 403
            ? 'You are not authorized to start this auction.'
            : error.status === 409
              ? 'This auction can no longer be started. Refresh the listings.'
              : 'Unable to start this auction.');

        console.error('Failed to start auction:', error);
        this.loadAuctions();
      }
    });
  }

  // -------------------------
  // CLOSE AUCTION
  // -------------------------

  closeAuction(auction: Auction): void {
    if (!this.currentUser || !this.authService.getToken()) {
      this.openAuthForm('login');
      this.authError = 'Please log in to manage auctions.';
      return;
    }

    if (!this.canCloseAuction(auction)) {
      this.errorMessage = this.isAuctionExpired(auction)
        ? 'This auction has expired. It can no longer be closed manually.'
        : 'Only live auctions can be closed.';

      return;
    }

    const id = this.getAuctionId(auction);

    if (!id) {
      this.errorMessage = 'Auction ID is missing.';
      return;
    }

    if (!confirm(`Are you sure you want to close "${auction.title}"?`)) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<Auction>(`${this.apiUrl}/${id}/close`, {}).subscribe({
      next: () => {
        this.successMessage = 'Auction closed successfully.';
        this.loadAuctions();
      },
      error: error => {
        this.errorMessage =
          error.error?.message ||
          (error.status === 403
            ? 'You are not authorized to close this auction.'
            : error.status === 409
              ? 'This auction can no longer be closed. Refresh the listings.'
              : 'Unable to close this auction.');

        console.error('Failed to close auction:', error);
        this.loadAuctions();
      }
    });
  }

  // -------------------------
  // BIDDING
  // -------------------------

  minimumNextBid(auction: Auction): number {
    const currentPrice = Number(this.getDisplayPrice(auction) ?? 0);
    const increment = Number(auction.minimumIncrement ?? 0);
    return Number((currentPrice + increment).toFixed(2));
  }

  placeBid(auction: Auction): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.currentUser || !this.authService.getToken()) {
      this.openAuthForm('login');
      this.authError = 'Log in to participate in this auction.';
      return;
    }

    const auctionId = this.getAuctionId(auction);
    if (!auctionId) {
      this.errorMessage = 'Auction ID is missing.';
      return;
    }

    if (this.getEffectiveStatus(auction) !== 'LIVE') {
      this.errorMessage = 'Only live auctions accept bids.';
      return;
    }

    const endTime = auction.endsAt ? new Date(auction.endsAt).getTime() : NaN;
    if (!Number.isFinite(endTime) || endTime <= Date.now()) {
      this.errorMessage = 'This auction has ended and cannot accept bids.';
      this.loadAuctions();
      return;
    }

    const amount = Number(this.bidAmounts[auctionId]);
    const minimum = this.minimumNextBid(auction);
    if (!Number.isFinite(amount) || amount < minimum) {
      this.errorMessage = `Your bid must be at least ₹${minimum.toLocaleString('en-IN')}.`;
      return;
    }

    if (this.submittingBidIds.has(auctionId)) return;

    this.submittingBidIds.add(auctionId);
    const payload = { amount, requestId: this.createRequestId() };

    this.http.post<Bid>(`${this.apiUrl}/${auctionId}/bids`, payload).subscribe({
      next: response => {
        this.submittingBidIds.delete(auctionId);
        delete this.bidAmounts[auctionId];
        this.successMessage = `Your bid of ₹${amount.toLocaleString('en-IN')} was placed successfully.`;
        this.loadAuctions();
        this.loadBidHistory(auctionId);
      },
      error: error => {
        this.submittingBidIds.delete(auctionId);
        this.errorMessage = error?.error?.message ||
          (error.status === 401 ? 'Your session has expired. Please log in again.' :
          error.status === 403 ? 'You are not authorized to place this bid.' :
          error.status === 404 ? 'This auction could not be found.' :
          error.status === 409 ? 'The auction or current price changed. Refresh and try again.' :
          error.status === 400 ? 'The bid was rejected. Check the minimum amount and auction status.' :
          'Unable to place your bid. Please try again.');
        console.error('Failed to place bid:', error);
        this.loadAuctions();
      }
    });
  }

  toggleBidHistory(auction: Auction): void {
    const auctionId = this.getAuctionId(auction);
    if (!auctionId) return;

    const visible = !this.bidHistoryVisible[auctionId];
    this.bidHistoryVisible[auctionId] = visible;
    if (visible && !this.bidHistoryByAuction[auctionId]) {
      this.loadBidHistory(auctionId);
    }
  }

  loadBidHistory(auctionId: string): void {
    this.http.get<Bid[]>(`${this.apiUrl}/${auctionId}/bids`).subscribe({
      next: bids => {
        this.bidHistoryByAuction[auctionId] = Array.isArray(bids) ? bids : [];
      },
      error: error => {
        console.error('Failed to load bid history:', error);
        this.bidHistoryByAuction[auctionId] = [];
      }
    });
  }

  private createRequestId(): string {
    // Compatible with browsers where crypto.randomUUID is unavailable.
    if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
      return crypto.randomUUID();
    }
    return `${Date.now()}-${Math.random().toString(16).slice(2)}-${Math.random().toString(16).slice(2)}`;
  }
}
