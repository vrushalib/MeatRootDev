package com.konakart.app;

import java.util.ArrayList;
import java.util.List;

import com.konakart.appif.*;
import com.konakart.db.KKBasePeer;
import com.workingdogs.village.Record;

/**
 *  The KonaKart Custom Engine - GetCategoryTree - Generated by CreateKKCustomEng
 */
@SuppressWarnings("all")
public class GetCategoryTree
{
    KKEng kkEng = null;

    /**
     * Constructor
     */
     public GetCategoryTree(KKEng _kkEng)
     {
         kkEng = _kkEng;
     }

     public CategoryIf[] getCategoryTree(int languageId, boolean getNumProducts) throws KKException
     {
         return kkEng.getCategoryTree(languageId, getNumProducts);
     }
     
 	public static List<CategoryIf> getAllInvisibleCategories() {
		List<CategoryIf> categories = null;
		try {
			List<Record> records = KKBasePeer
					.executeQuery("SELECT cats.categories_id, categories_name, categories_image from categories cats join categories_description description on cats.categories_id = description.categories_id where cats.parent_id = 0 and categories_invisible = 1 and language_id = 1 ");

			if (records == null || records.size() == 0) {
				return null;
			}
			categories = new ArrayList<CategoryIf>();
			for (int i = 0; i < records.size(); i++) {
				CategoryIf cat = new Category();
				cat.setId(records.get(i).getValue(1).asInt());
				cat.setName(records.get(i).getValue(2).toString());
				cat.setImage(records.get(i).getValue(3).toString());
				categories.add(cat);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return categories;
	}
}
