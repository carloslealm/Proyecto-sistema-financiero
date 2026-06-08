export type EstadoPrestamo =
  | 'SOLICITADO' | 'EN_REVISION' | 'APROBADO' | 'RECHAZADO'
  | 'DESEMBOLSADO' | 'AL_DIA' | 'EN_MORA' | 'CANCELADO' | 'CASTIGADO';
 
export type SistemaAmortizacion = 'FRANCES' | 'ALEMAN' | 'AMERICANO';
 
export type EstadoCuota =
  | 'PENDIENTE' | 'PAGADA' | 'PAGADA_PARCIAL' | 'EN_MORA' | 'CONDONADA';
 
export interface Prestamo {
  id: number;
  clienteId: number;
  clienteNombre: string;
  clienteCedula: string;
  asesorId: number;
  asesorNombre: string;
  monto: number;
  tasaInteres: number;
  plazoMeses: number;
  sistemaAmortizacion: SistemaAmortizacion;
  estado: EstadoPrestamo;
  fechaSolicitud: string;
  fechaAprobacion?: string;
  fechaDesembolso?: string;
  fechaCancelacion?: string;
  totalAPagar?: number;
  totalInteres?: number;
  observaciones?: string;
  totalCuotas: number;
  cuotasPagadas: number;
  cuotasPendientes: number;
  cuotasEnMora: number;
  saldoPendiente: number;
  planDePagos?: Cuota[];
  createdAt: string;
}
 
export interface PrestamoRequest {
  clienteId: number;
  monto: number;
  tasaInteres: number;
  plazoMeses: number;
  sistemaAmortizacion: SistemaAmortizacion;
  observaciones?: string;
}
 
export interface Cuota {
  id: number;
  numeroCuota: number;
  capital: number;
  interes: number;
  valorTotal: number;
  fechaVencimiento: string;
  saldoCapital: number;
  estado: EstadoCuota;
  estaVencida: boolean;
  diasMora: number;
  interesMoraEstimado: number;
}
 
export interface Dashboard {
  carteraTotal: number;
  carteraEnMora: number;
  recaudadoEsteMes: number;
  porcentajeMora: number;
  totalClientes: number;
  totalPrestamosActivos: number;
  totalPrestamosEnMora: number;
  cuotasVencidasHoy: number;
  prestamosPorEstado: { [key: string]: number };
  carteraPorMes: { [key: string]: number };
}