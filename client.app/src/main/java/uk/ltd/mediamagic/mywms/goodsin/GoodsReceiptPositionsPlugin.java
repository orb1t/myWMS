package uk.ltd.mediamagic.mywms.goodsin;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import de.linogistix.los.inventory.model.LOSGoodsReceiptPosition;
import de.linogistix.los.inventory.model.LOSGoodsReceiptState;
import de.linogistix.los.query.OrderByToken;
import de.linogistix.los.query.QueryDetail;
import de.linogistix.los.query.TemplateQuery;
import de.linogistix.los.query.TemplateQueryFilter;
import de.linogistix.los.query.TemplateQueryWhereToken;
import javafx.application.Platform;
import javafx.util.StringConverter;
import res.R;
import uk.ltd.mediamagic.flow.crud.BODTOPlugin;
import uk.ltd.mediamagic.flow.crud.BODTOTable;
import uk.ltd.mediamagic.flow.crud.SubForm;
import uk.ltd.mediamagic.fx.action.AC;
import uk.ltd.mediamagic.fx.action.RootCommand;
import uk.ltd.mediamagic.fx.controller.list.CellRenderer;
import uk.ltd.mediamagic.fx.controller.list.MaterialCells;
import uk.ltd.mediamagic.fx.converters.DateConverter;
import uk.ltd.mediamagic.fx.flow.ApplicationContext;
import uk.ltd.mediamagic.fx.flow.Flow;
import uk.ltd.mediamagic.fx.flow.ViewContextBase;
import uk.ltd.mediamagic.mywms.common.QueryUtils;
import uk.ltd.mediamagic.mywms.goodsout.GoodsOutUtils.OpenFilter;
import uk.ltd.mediamagic.mywms.inventory.PrintGoodsReceiptLabel;
import uk.ltd.mediamagic.mywms.transactions.StockUnitRecordAction;

@SubForm(
		title="Main", columns=1, 
		properties={
				"positionNumber", "goodsReceipt", "advice", "type", "itemData", 
				"lot", "amount", "qaLock", "qaFault", "unitLoad", "stockUnit"
			}
	)

public class GoodsReceiptPositionsPlugin  extends BODTOPlugin<LOSGoodsReceiptPosition> {

	private enum Action {PrintLabel, StockUnitLog}
	
	public GoodsReceiptPositionsPlugin() {
		super(LOSGoodsReceiptPosition.class);
	}
	
	@Override
	public String getPath() {
		return "{1, _Goods in} -> {1, _Goods Receipt Positions}";
	}
	
	@Override
	protected StringConverter<?> getConverter(PropertyDescriptor property) {
		if ("receiptDate".equals(property.getName())) return new DateConverter();
		return super.getConverter(property);
	}
	
	@Override
	public Supplier<CellRenderer<LOSGoodsReceiptPosition>> createCellFactory() {
		return MaterialCells.withID(s -> R.svgPaths.goodsWaiting(), 
				LOSGoodsReceiptPosition::getPositionNumber, 
				s -> String.format("Item: %s, x %f", s.getItemData(), s.getAmount()),
				s -> String.format("Lot: %s", s.getLot()),
				s -> String.format("U/Load No: %s", s.getUnitLoad()));
	}
	
	@Override
	protected void refresh(BODTOTable<LOSGoodsReceiptPosition> source, ViewContextBase context) {
		TemplateQuery template = source.createQueryTemplate();
		OpenFilter openFilter = QueryUtils.getFilter(source, OpenFilter.Open);
		if (openFilter == OpenFilter.Open) {
			TemplateQueryFilter filter = template.addNewFilter();
			
			filter.addWhereToken(QueryUtils.or(new TemplateQueryWhereToken(
					TemplateQueryWhereToken.OPERATOR_EQUAL, "goodsReceipt.receiptState", LOSGoodsReceiptState.RAW)));
			filter.addWhereToken(QueryUtils.or(new TemplateQueryWhereToken(
					TemplateQueryWhereToken.OPERATOR_EQUAL, "goodsReceipt.receiptState", LOSGoodsReceiptState.ACCEPTED)));
		}
		QueryDetail detail = source.createQueryDetail();
		if (detail.getOrderBy().size() == 0) {
			detail.getOrderBy().add(new OrderByToken("created", false));
		}
		source.clearTable();
		getListData(context, detail, template)
			.thenAcceptAsync(source::setLOSResultList, Platform::runLater);			
	}
	
	@Override
	public List<String> getTableColumns() {
		return Arrays.asList("id", 
				"name AS positionNumber",	"itemData", "lot", "amount", "qaLock","unitLoad");
	}
	
	@Override
	protected void configureCommands(RootCommand command) {
		super.configureCommands(command);
		command.begin(RootCommand.MENU)
			.add(AC.id(Action.StockUnitLog).text("Stock Unit Log"))
		.end();
	}
	
	@Override
	public Flow createNewFlow(ApplicationContext context) {
		Flow flow = super.createNewFlow(context);
		flow.globalWithSelection()
			.withMultiSelection(Action.PrintLabel, new PrintGoodsReceiptLabel())
			.withSelection(Action.StockUnitLog, StockUnitRecordAction.forActivityCode())
		.end();
		return flow;
	}
	
	@Override
	protected BODTOTable<LOSGoodsReceiptPosition> getTable(ViewContextBase context) {
		BODTOTable<LOSGoodsReceiptPosition> table = super.getTable(context);
		QueryUtils.addFilter(table, OpenFilter.Open, () -> refresh(table, context));
		table.getCommands()
			.menu(RootCommand.MENU_PRINT)
				.add(AC.id(Action.PrintLabel).text("Print Label"))
			.end()
		.end();
		return table;
	}
	
}
