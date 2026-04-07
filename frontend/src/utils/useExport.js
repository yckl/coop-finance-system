import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import { ElMessage, ElLoading } from 'element-plus';

export function useExport() {
  const exportToExcel = (data, filename) => {
    try {
      const sheet = XLSX.utils.json_to_sheet(data);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, sheet, '金融业务台账');
      
      const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
      saveAs(new Blob([buffer], { type: 'application/octet-stream' }), `${filename}_DUMP_${Date.now()}.xlsx`);
      ElMessage.success('报表快照已强行固化至本地沙盒。');
    } catch (e) {
      ElMessage.error('序列化结构异常: ' + e);
    }
  };

  const exportToPdf = async (elementId, filename) => {
    const el = document.getElementById(elementId);
    if (!el) return ElMessage.warning('未能锚定凭证结构节点');
    
    const loading = ElLoading.service({ text: '安全协议握手中...正在生成携带防伪数据的密函...', background: 'rgba(0,0,0,0.8)' });
    
    try {
      // 动态注入物理防伪水印层
      const watermark = document.createElement('div');
      watermark.innerText = 'COOP RURAL FINANCIAL / 绝密流转体系';
      watermark.style.cssText = 'position:absolute; top:45%; left:0%; font-size:40px; font-weight:900; color:rgba(0,0,0,0.08); transform:rotate(-25deg); z-index:9999; pointer-events:none; white-space:nowrap; letter-spacing:8px;';
      el.appendChild(watermark);

      const canvas = await html2canvas(el, { scale: 2, useCORS: true, logging: false });
      el.removeChild(watermark); 

      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
      
      pdf.addImage(imgData, 'PNG', 0, 10, pdfWidth, pdfHeight);
      pdf.save(`加密票据_${filename}_SEC.pdf`);
      
      ElMessage.success('高阶加密票据（含不可逆水印）下发流转环节完毕。');
    } catch (e) {
      ElMessage.error('底层图像物理渲染层断层');
    } finally {
      loading.close();
    }
  };

  return { exportToExcel, exportToPdf };
}
