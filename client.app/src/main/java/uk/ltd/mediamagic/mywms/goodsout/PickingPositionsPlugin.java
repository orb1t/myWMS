package uk.ltd.mediamagic.mywms.goodsout;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import de.linogistix.los.inventory.model.LOSPickingPosition;
import de.linogistix.los.query.BODTO;
import de.linogistix.los.query.QueryDetail;
import de.linogistix.los.query.TemplateQuery;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import javafx.util.StringConverter;
import uk.ltd.mediamagic.flow.crud.BODTOPlugin;
import uk.ltd.mediamagic.flow.crud.SubForm;
import uk.ltd.mediamagic.fx.controller.list.MaterialListItems;
import uk.ltd.mediamagic.fx.converters.DateConverter;
import uk.ltd.mediamagic.fx.converters.MapConverter;
import uk.ltd.mediamagic.fx.flow.ContextBase;

@SubForm(
		title="Main", columns=1, 
		properties={"number", "externalNumber", "externalId", "state", "strategy", "destination", "prio"}
	)
@SubForm(
		title="Delivery", columns=2, 
		properties={"customerNumber", "customerName", "delivery", "documentUrl", "labelUrl", "dtype"}
	)
public class PickingPositionsPlugin  extends BODTOPlugin<LOSPickingPosition> {

	public PickingPositionsPlugin() {
		super(LOSPickingPosition.class);
	}
	
	@Override
	public String getPath() {
		return "{1, _Goods out} -> {2, _Picking order positions}";
	}
	
	@Override
	protected StringConverter<?> getConverter(PropertyDescriptor property) {
		if ("delivery".equals(property.getName())) return new DateConverter();
		else if ("prio".equals(property.getName())) return new MapConverter<Integer>(GoodsOutTypes.priority);
		else if ("state".equals(property.getName())) return new MapConverter<Integer>(GoodsOutTypes.state);
		return super.getConverter(property);
	}
	
	@Override
	public Callback<ListView<LOSPickingPosition>, ListCell<LOSPickingPosition>> createListCellFactory() {
		return MaterialListItems.withID(s -> GoodsOutUtils.getIcon(s.getState()), 
				s -> s.toUniqueString(), 
				s -> String.format("%s, %s", s.getItemData().getNumber(), s.getItemData().getName()),
				s -> String.format("%s", GoodsOutTypes.state.getValue(s.getState())),
				s -> String.format("%s of %s", s.getAmountPicked(), s.getAmount()));
	}
		
	@Override
	public CompletableFuture<List<BODTO<LOSPickingPosition>>> 
	getListData(ContextBase context, QueryDetail detail, TemplateQuery template) {
		if (detail.getOrderBy().isEmpty()) {
			detail.addOrderByToken("created", false);
		}
		return super.getListData(context, detail, template);
	}

	@Override
	public List<String> getTableColumns() {
		return Arrays.asList("id", 
				"name AS number",	"pickingOrderNumber", 
				"itemDataNumber AS itemData.number", "itemDataName AS itemData.name", 
				"amount", "pickFromLocationName", "state");
	}
	
}
